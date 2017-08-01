# geode-partitioning-demo
---------------------------------------------
## Build a demo
   ```
   geode-partitioning-demo (master) $ mvn clean package
   ```
   This create a jar for the demo module: ./geode-partitioning-demo/target/geode-partitioning-demo-1.0-SNAPSHOT.jar
   Which needs to be deploy into cache-server's classpath as outline below.

## setup a geode cluster and create server region

1. launch the geode CLI
   ```
   apache-geode/bin (master) $ ./gfsh
   ```

2. start a geode locator
    ```
    gfsh>start locator --name=l1
    ```
3. start a geode cache server1
    ```
    gfsh>start server --name=s1 --server-port=40404
    ```
4. start a geode cache server2
   ```
    gfsh>start server --name=s2 --server-port=40405
    ```
5. start a geode cache server3
    ```
    gfsh>start server --name=s3 --server-port=40406
    ```
6. Add a demo related classes (function that calculate bucket stats, Objects used as key and value etc) into cache-server's classpath
    ```
    gfsh>deploy --jar=<PATH>/geode-partitioning-demo/target/geode-partitioning-demo-1.0-SNAPSHOT.jar
    ```
7. Create server region, Note the type and redundancy config,
    ```
    gfsh>create region --name=customer-region --type=PARTITION --redundant-copies=1
    ```
8. Describer the newly created region, verify the region attributes
    ```
    gfsh>describe region --name=/customer-region
    ..........................................................
    Name            : customer-region
    Data Policy     : partition
    Hosting Members : s3
                      s1
                      s2

    Non-Default Attributes Shared By Hosting Members

    Type    |       Name       | Value
    --------- | ---------------- | ---------
    Region    | size             | 0
              | data-policy      | PARTITION
    Partition | redundant-copies | 1
    ```
9. If you modify the function or model objects, you need to update (deploy) the jar in the server classpath.
    ```
    gfsh>undeploy --jar=<PATH>/geode-partitioning-demo/target/geode-partitioning-demo-1.0-SNAPSHOT.jar
    gfsh>deploy --jar=<PATH>/geode-partitioning-demo/target/geode-partitioning-demo-1.0-SNAPSHOT.jar
    ```

## Poor hashcode implementation that results in a data skew
    ```
    /**
     * calculate hashcode using firstname only.
     */
    @Override
    public int hashCode() {
        return this.lastName.hashCode();
    }

    .........
    .........

    Output with above hashcode:
    Note: All 30 keys are ingested into single bucket.

    Counted 30 keys in region customer-region
    ###  18 -- {192.168.3.126:40406,30}  -- [{192.168.3.126:40405,30}]
    ```

## Improved hashcode implementation with near uniform distribution.

    ```
    /**
    * calculate hashcode using firstname, lastname and address
    */
    @Override
    public int hashCode() {
        return this.firstName.hashCode() + this.lastName.hashCode() + this.address.hashCode();
    }

    .........
    .........

    Output with above hashcode:
    Output format:

    Note: All 30 keys are ingested/distributed near uniformally.

    Counted 30 keys in region customer-region
    ### bucketID  {primaryNode,#keys}   -- list{secondaryNode,#keys}
    ###   0 -- {192.168.3.126:40404,2}  -- [{192.168.3.126:40406,2}]
    ###  67 -- {192.168.3.126:40404,2}  -- [{192.168.3.126:40406,2}]
    ###   4 -- {192.168.3.126:40404,2}  -- [{192.168.3.126:40405,2}]
    ###  70 -- {192.168.3.126:40404,2}  -- [{192.168.3.126:40406,2}]
    ###   6 -- {192.168.3.126:40404,2}  -- [{192.168.3.126:40405,2}]
    ###   9 -- {192.168.3.126:40405,2}  -- [{192.168.3.126:40406,2}]
    ###  78 -- {192.168.3.126:40406,2}  -- [{192.168.3.126:40405,2}]
    ###  17 -- {192.168.3.126:40406,2}  -- [{192.168.3.126:40405,2}]
    ###  82 -- {192.168.3.126:40404,4}  -- [{192.168.3.126:40406,4}]
    ###  19 -- {192.168.3.126:40405,2}  -- [{192.168.3.126:40406,2}]
    ###  88 -- {192.168.3.126:40405,2}  -- [{192.168.3.126:40406,2}]
    ###  91 -- {192.168.3.126:40406,2}  -- [{192.168.3.126:40404,2}]
    ###  92 -- {192.168.3.126:40406,2}  -- [{192.168.3.126:40404,2}]
    ###  31 -- {192.168.3.126:40405,2}  -- [{192.168.3.126:40404,2}]
    ###  32 -- {192.168.3.126:40405,2}  -- [{192.168.3.126:40406,2}]
    ###  98 -- {192.168.3.126:40405,2}  -- [{192.168.3.126:40404,2}]
    ###  37 -- {192.168.3.126:40406,2}  -- [{192.168.3.126:40404,2}]
    ###  39 -- {192.168.3.126:40406,2}  -- [{192.168.3.126:40404,2}]
    ### 103 -- {192.168.3.126:40405,2}  -- [{192.168.3.126:40404,2}]
    ###  42 -- {192.168.3.126:40404,2}  -- [{192.168.3.126:40406,2}]
    ### 107 -- {192.168.3.126:40406,6}  -- [{192.168.3.126:40405,6}]
    ###  47 -- {192.168.3.126:40404,2}  -- [{192.168.3.126:40405,2}]
    ### 111 -- {192.168.3.126:40405,2}  -- [{192.168.3.126:40404,2}]
    ###  48 -- {192.168.3.126:40404,2}  -- [{192.168.3.126:40405,2}]
    ###  52 -- {192.168.3.126:40406,2}  -- [{192.168.3.126:40405,2}]
    ###  60 -- {192.168.3.126:40405,4}  -- [{192.168.3.126:40404,4}]
    ```