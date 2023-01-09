/***
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.*;
***/
import model.Customer;
import model.Order;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * HCPartitioning, Hazelcast client module to test default partitioning.
 *
 * Created by: Nilkanth Patel
 */
public class HCPartitioning {
  public static void main(String[] args){
    /****
    Config config = new Config();
    //HazelcastInstance h = Hazelcast.newHazelcastInstance(config);
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.getGroupConfig().setName("dev").setPassword("dev-pass");
    clientConfig.getNetworkConfig().addAddress("localhost", "localhost:5702");

    //connect to hazelcast instance
    HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
    IMap<Customer, Order> mapCustomers = client.getMap("customers"); //creates the map proxy
    */

    /***
    mapCustomers.put(new Customer("Rahul", "303, Park avenue, sector-30, new delhi"), new Order("1", Arrays.asList("item-1", "item-2", "item-3")));
    mapCustomers.put(new Customer("Nilkanth", "A-703, Nandan Euphora, Pune"), new Order("1", Arrays.asList("item-7", "item-8", "item-9")));
    */

    /***
    ConcurrentMap<String, String> map = h.getMap("my-distributed-map");

    Set<Member> members = h.getCluster().getMembers();
    for(Member node : members){
      System.out.println("HCPartitioning.main memebr name=" + node.getUuid() + "Attrs = " + node.getAttributes());
    }

    map.put("k1", "v1");
    map.put("k2", "v2");
    map.put("k3", "v3");

    map.get("k1");

    Set<Partition> parts = h.getPartitionService().getPartitions();
    for(Partition p : parts){
      System.out.println("HCPartitioning.mainPartitionInfo = "+ p.toString());
    }
    h.shutdown();
    */
  }
}
