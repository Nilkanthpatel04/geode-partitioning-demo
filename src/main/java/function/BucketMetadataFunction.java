package function;

import model.Pair;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.distributed.internal.ServerLocation;
import org.apache.geode.internal.cache.BucketRegion;
import org.apache.geode.internal.cache.BucketServerLocation66;
import org.apache.geode.internal.cache.PartitionedRegion;
import org.apache.geode.internal.logging.LogService;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * BucketMetadataFunction defines a {@link Function} implementation to fetch the bucket statistics
 * Current implementation gives per bucket information like
 * Created by: Nilkanth Patel
 */
public class BucketMetadataFunction implements Function, Serializable {
  private static final Logger logger = LogService.getLogger();

  private static final long serialVersionUID = -3150906692851375834L;

  @Override
  public void execute(FunctionContext fc) {
    Cache cache = CacheFactory.getAnyInstance();
    final String regionName = (String) fc.getArguments();
    if (regionName == null) {
      throw new IllegalArgumentException("Region name must be provided.");
    }

    Region region = cache.getRegion(regionName);
    if (region == null) {
      logger.info("BucketMetadataFunction: Region does not exist: {}", regionName);
      Map<Integer, Pair<ServerLocation, Long>> primaryMap = Collections.emptyMap();
      Map<Integer, Pair<ServerLocation, Long>> secondaryMap = Collections.emptyMap();
      fc.getResultSender().lastResult(new Object[] { primaryMap, secondaryMap });
      return;
    }
    if (!(region instanceof PartitionedRegion)) {
      throw new IllegalArgumentException("Supported only for PartitionedRegion.");
    }

    Map<Integer, Pair<ServerLocation, Long>> primaryMap = new HashMap<>();
    Map<Integer, Set<Pair<ServerLocation, Long>>> secondaryMap = new HashMap<>();
    Map<String, ServerLocation> locations = new HashMap<>();
    Map<ServerLocation, List<Integer>> serverToBucketMap = new HashMap<>();

    PartitionedRegion prRgion = (PartitionedRegion) region;
    Map<Integer, List<BucketServerLocation66>> bucketToServerLocations =
        prRgion.getRegionAdvisor().getAllClientBucketProfiles();

    for (List<BucketServerLocation66> serverLocations : bucketToServerLocations.values()) {
      for (BucketServerLocation66 bl : serverLocations) {

        ServerLocation location = locations.get(bl.getHostName() + bl.getPort());
        if (location == null) {
          location = new ServerLocation(bl.getHostName(), bl.getPort());
          locations.put(bl.getHostName() + bl.getPort(), location);
        }
        BucketRegion br = prRgion.getDataStore().getLocalBucketById(bl.getBucketId());
        long size = 0;
        if (br != null) {
          size = prRgion.getDataStore().getLocalBucketById(bl.getBucketId()).size();
        }

        //bucket not hosted on this node, hence continue.
        if ( size == 0 ) {
          continue;
        }

        //logger.info("[ bucketServerlocation = " + bl  + ", bucketId = " + bl.getBucketId() +"]");
        if (bl.isPrimary()) {
          primaryMap.put(bl.getBucketId(), new Pair<>(location, size));
          if (!serverToBucketMap.containsKey(location)) {
            serverToBucketMap.put(location, new ArrayList<>());
          }
          serverToBucketMap.get(location).add(bl.getBucketId());
        } else {
          Set<Pair<ServerLocation, Long>> secServerLocations = secondaryMap.get(bl.getBucketId());
          if (secServerLocations == null) {
            secServerLocations = new LinkedHashSet<>();
            secServerLocations.add(new Pair<>(location, size));
            secondaryMap.put(bl.getBucketId(), secServerLocations);
          } else {
            secServerLocations.add(new Pair<>(location, size));
          }
        }
      }
    }
    fc.getResultSender().lastResult(new Object[] { primaryMap, secondaryMap, serverToBucketMap });
  }

  /*@Override
  public String getId() {
    return null;
  }*/

  @Override
  public boolean optimizeForWrite() {
    return true;
  }

  @Override
  public boolean isHA() {
    return false;
  }
}
