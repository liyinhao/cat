package com.dianping.cat.status;

import com.dianping.cat.status.model.entity.DruidPoolInfo;
import com.dianping.cat.status.model.entity.MongoPoolInfo;
import com.dianping.cat.status.model.entity.RedisPoolInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by liyinhao on 18/8/23.
 */
class ThreadsInfoCollector {

    public static List<DruidPoolInfo> getDruidPools() {

        List<DruidPoolInfo> result = new ArrayList<DruidPoolInfo>();

        try {

            String[] attributes = {"Name", "Url", "PoolingCount", "PoolingPeak", "ActiveCount", "ActivePeak"};
            List<Map<String, Object>> infos
                    = JMXQuery.queryList("com.alibaba.druid", "DruidDataSource", attributes);

            for (Map<String, Object> map : infos) {

                try {
                    DruidPoolInfo druidPoolInfo = new DruidPoolInfo();
                    druidPoolInfo.setName(map.get("Name").toString());
                    druidPoolInfo.setUrl(map.get("Url").toString());
                    druidPoolInfo.setPoolingCount((Integer) map.get("PoolingCount"));
                    druidPoolInfo.setPoolingPeak((Integer) map.get("PoolingPeak"));
                    druidPoolInfo.setActiveCount((Integer) map.get("ActiveCount"));
                    druidPoolInfo.setActivePeak((Integer) map.get("ActivePeak"));

                    result.add(druidPoolInfo);
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return result;
    }

    public static List<RedisPoolInfo> getRedisPools() {

        List<RedisPoolInfo> result = new ArrayList<RedisPoolInfo>();

        try {

            String[] attributes = {"NumActive", "NumIdle", "FactoryType", "MaxTotal", "MaxIdle", "MinIdle"};

            Map<String, Map<String, Object>> infos =
                    JMXQuery.queryMap("org.apache.commons.pool2", "GenericObjectPool", attributes);

            for (Map.Entry<String, Map<String, Object>> entry : infos.entrySet()) {

                try {
                    String entryKey = entry.getKey();
                    Map<String, Object> entryValue = entry.getValue();

                    String factoryType = entryValue.get("FactoryType").toString();
                    if (factoryType != null && factoryType.startsWith("redis.clients.jedis.JedisFactory")) {

                        int numActive = (Integer) entryValue.get("NumActive");
                        int numIdle = (Integer) entryValue.get("NumIdle");
                        int maxTotal = (Integer) entryValue.get("MaxTotal");
                        int maxIdle = (Integer) entryValue.get("MaxIdle");
                        int minIdle = (Integer) entryValue.get("MinIdle");


                        RedisPoolInfo redisPoolInfo = new RedisPoolInfo();
                        redisPoolInfo.setName(entryKey);
                        redisPoolInfo.setCount(numActive);
                        redisPoolInfo.setNumActive(numActive);
                        redisPoolInfo.setNumIdle(numIdle);
                        redisPoolInfo.setMaxTotal(maxTotal);
                        redisPoolInfo.setMaxIdle(maxIdle);
                        redisPoolInfo.setMinIdle(minIdle);

                        result.add(redisPoolInfo);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return result;
    }

    public static List<MongoPoolInfo> getMongoPools(){
        List<MongoPoolInfo> result = new ArrayList<MongoPoolInfo>();

        try {

            String[] attributes = {"Host", "MaxSize", "MinSize", "Port", "Size", "WaitQueueSize"};

            List<Map<String, Object>> infos
                    = JMXQuery.queryList("org.mongodb.driver", "ConnectionPool", attributes);
            for (Map<String, Object> info : infos) {
                try {

                    String host = info.get("Host").toString();
                    String port = info.get("Port").toString();

                    Integer size = (Integer) info.get("Size");
                    Integer minSize = (Integer) info.get("MinSize");
                    Integer maxSize = (Integer) info.get("MaxSize");
                    Integer waitQueueSize = (Integer) info.get("WaitQueueSize");

                    MongoPoolInfo mongoPoolInfo = new MongoPoolInfo();

                    mongoPoolInfo.setName(host + ":" + port);
                    mongoPoolInfo.setCount(size);
                    mongoPoolInfo.setMinSize(minSize);
                    mongoPoolInfo.setMaxSize(maxSize);
                    mongoPoolInfo.setWaitQueueSize(waitQueueSize);

                    result.add(mongoPoolInfo);
                }catch (Exception e){
                    continue;
                }
            }
        }catch (Exception e){
            // ignore
        }

        return result;
    }

}
