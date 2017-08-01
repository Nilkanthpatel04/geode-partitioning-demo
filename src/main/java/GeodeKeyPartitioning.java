import function.BucketMetadataFunction;
import model.Customer;
import model.Order;
import model.Pair;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.distributed.internal.ServerLocation;
import org.apache.geode.internal.Assert;

import java.io.Serializable;
import java.util.*;

/**
 * GeodeKeyPartitioning, Geode client module to test default partitioning.
 *
 * Created by: Nilkanth Patel
 */
public class GeodeKeyPartitioning {

  public void testDefaultPartitioning(){
    ClientCache cache = new ClientCacheFactory().addPoolLocator("127.0.0.1", 10334)
        .set("log-level", "WARN").create();

    final String regionName = "customer-region";

    // create a local region that matches the server region
    Region<Customer, Order> region = cache.<Customer, Order>createClientRegionFactory(ClientRegionShortcut.CACHING_PROXY)
        .create(regionName);

    // insert values into the region
    Arrays.stream(TestData.names).forEach(name -> {
      String[] nameSplits = name.split("\\s+");
      //System.out.println("NNN GeodeKeyPartitioning [ FN = " + nameSplits[0] + " LN=" + nameSplits[1] + "]");
      Assert.assertTrue(nameSplits.length == 2);
      Customer key = new Customer(nameSplits[0], nameSplits[1], "A 703 Nandan Euphora Vishrantwadi Pune");
      Order val = new Order("ord-1",  Arrays.asList("item-7", "item-8", "item-9"));
      region.put(key, val);
    });

    // count the values in the region
    int inserted = region.keySetOnServer().size();
    System.out.println(String.format("Counted %d keys in region %s", inserted, region.getName()));

    Map<Integer, Pair<ServerLocation, Long>> primaryMap = new HashMap<>();
    Map<Integer,Set<Pair<ServerLocation,Long>>>secondaryMap = new HashMap<>();


    //Get Bucket statistics
    //How to interpret Output format:
    //###  PartitionID  --  {PrimaryHost:port, #keys}        {SecondaryHost:port, #keys}
    //###  18           --  {192.168.3.126:40406,30}     -- [{192.168.3.126:40406,30}, {192.168.3.126:40405,30}]
    List<Object[]> output =
        (List<Object[]>)FunctionService.onServers(region.getRegionService()).setArguments(regionName).execute(new BucketMetadataFunction()).getResult();

    Map<ServerLocation, List<Integer>> serverToBucketMap = new HashMap<>(10);

    for (Object[] objects : ((List<Object[]>) output)) {
      primaryMap.putAll((Map) objects[0]);
      if (objects.length > 2) {
        serverToBucketMap.putAll((Map) objects[2]);
      }
      if (secondaryMap != null) {
        Map<Integer, Set<Pair<ServerLocation, Long>>> map = (Map) objects[1];
        for (Map.Entry<Integer, Set<Pair<ServerLocation, Long>>> entry : map.entrySet()) {
          if (secondaryMap.containsKey(entry.getKey())) {
            secondaryMap.get(entry.getKey()).addAll(entry.getValue());
          } else {
            secondaryMap.put(entry.getKey(), entry.getValue());
          }
        }
      }
    }


    /*
    for (Object[] objects : ((List<Object[]>) output)) {
      System.out.println("Object[0] => " + objects[0]);
      System.out.println("Object[1] => " + objects[1]);
      primaryMap.putAll((Map) objects[0]);

      if (secondaryMap != null) {
        Map<Integer, Set<Pair<ServerLocation, Long>>> map = (Map) objects[1];
        for (Map.Entry<Integer, Set<Pair<ServerLocation, Long>>> entry : map.entrySet()) {
          if (secondaryMap.containsKey(entry.getKey())) {
            secondaryMap.get(entry.getKey()).addAll(entry.getValue());
          } else {
            secondaryMap.put(entry.getKey(), entry.getValue());
          }
        }
      }

    }
    */

    for (Integer key: primaryMap.keySet()) {
      System.out.printf("### %3d -- %s  -- %s\n", key, primaryMap.get(key), secondaryMap.get(key));
    }
  }
}
