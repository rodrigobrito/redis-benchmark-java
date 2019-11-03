package br.com.viavarejo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.Utf8StringCodec;
import io.lettuce.core.masterslave.MasterSlave;
import io.lettuce.core.masterslave.StatefulRedisMasterSlaveConnection;
import redis.clients.jedis.*;

import java.io.*;
import java.util.*;

public final class RedisConnectionManagement {
    private static final RedisConnectionManagement connectionManagement = new RedisConnectionManagement();
    private StatefulConnection<String, String> lettuceConnection;
    private JedisCommands jedisCommands;

    private RedisConnectionManagement() {
    }

    private Properties getProperties() {
        Properties prop = new Properties();
        try {
            File f = new File("config.cfg");
            if (f.exists()) {
                prop.load(new FileInputStream("config.cfg"));
            } else {
                System.out.println("Please create config.cfg properties file and then execute the program!");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }

    private String getConnectionString() {
        Properties properties = getProperties();
        return properties.getProperty("redis.connection");
    }

    private String getSentinelMasterName() {
        Properties properties = getProperties();
        return properties.getProperty("redis.sentinel.master.group.name");
    }

    private boolean isSentinel() {
        String redisConnection = getConnectionString();
        return redisConnection.contains("sentinel");
    }

    private List<RedisURI> getRedisUris() {
        String redisConnection = getConnectionString();
        redisConnection = redisConnection.replace("redis-sentinel://", "")
                .replace("redis://", "");
        List<RedisURI> uris = new ArrayList<>();
        String[] nodes = redisConnection.split(",");
        for (String node : nodes) {
            String[] hostAndPort = node.split(":");
            String host = hostAndPort[0];
            String port = hostAndPort[1];
            RedisURI uri = RedisURI.create(host, Integer.parseInt(port));
            uris.add(uri);
        }
        return uris;
    }

    private StatefulConnection<String, String> createLettuceConnection() {
        try {
            List<RedisURI> uris = getRedisUris();

            // Standalone
            if (uris.size() == 1) {
                RedisClient client = RedisClient.create(uris.get(0));
                return client.connect();
            }

            // Sentinel
            if (isSentinel()) {
                RedisURI firstUri = uris.get(0);
                RedisURI.Builder builder = RedisURI.Builder.sentinel(firstUri.getHost(), firstUri.getPort(), getSentinelMasterName());
                for (int i = 1; i < uris.size(); i++) {
                    RedisURI currentUri = uris.get(i);
                    builder = builder.withSentinel(currentUri.getHost(), currentUri.getPort());
                }
                RedisURI finalUri = builder.build();
                RedisClient redisClient = RedisClient.create(finalUri);
                return redisClient.connect();
            }

            // Cluster
            RedisClusterClient clusterClient = RedisClusterClient.create(uris);
            ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                    .enablePeriodicRefresh()
                    .build();

            clusterClient.setOptions(ClusterClientOptions.builder()
                    .topologyRefreshOptions(topologyRefreshOptions)
                    .build());

            return clusterClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private StatefulConnection<String, String> getLettuceConnection() {
        if (lettuceConnection == null) {
            lettuceConnection = createLettuceConnection();
        }
        return lettuceConnection;
    }

    private RedisStringAsyncCommands<String, String> getLettuceStringAsyncCommands() {
        StatefulConnection conn = getLettuceConnection();

        if (conn instanceof StatefulRedisClusterConnection) {
            StatefulRedisClusterConnection<String, String> cluster = ((StatefulRedisClusterConnection<String, String>) conn);
            return cluster.async();
        }

        if (conn instanceof StatefulRedisMasterSlaveConnection) {
            StatefulRedisMasterSlaveConnection<String, String> sentinel = ((StatefulRedisMasterSlaveConnection<String, String>) conn);
            return sentinel.async();
        }

        StatefulRedisConnection<String, String> defaultConnection = ((StatefulRedisConnection<String, String>) getLettuceConnection());
        return defaultConnection.async();
    }

    private RedisStringReactiveCommands<String, String> getLettuceStringReactiveCommands() {
        StatefulConnection conn = getLettuceConnection();

        if (conn instanceof StatefulRedisClusterConnection) {
            StatefulRedisClusterConnection<String, String> cluster = ((StatefulRedisClusterConnection<String, String>) conn);
            return cluster.reactive();
        }

        if (conn instanceof StatefulRedisMasterSlaveConnection) {
            StatefulRedisMasterSlaveConnection<String, String> sentinel = ((StatefulRedisMasterSlaveConnection<String, String>) conn);
            return sentinel.reactive();
        }
        StatefulRedisConnection<String, String> defaultConnection = ((StatefulRedisConnection<String, String>) getLettuceConnection());
        return defaultConnection.reactive();
    }

    private JedisCommands createJedisConnection() {
        try {
            List<RedisURI> uris = getRedisUris();

            // Standalone
            if (uris.size() == 1) {
                RedisURI redisUri = uris.get(0);
                String redisHost = redisUri.getHost();
                int redisPort = redisUri.getPort();
                return new Jedis(redisHost, redisPort);
            }

            // Sentinel
            if (isSentinel()) {
                Set sentinels = new HashSet();
                for (RedisURI redisUri : uris) {
                    sentinels.add(String.format("%s:%s", redisUri.getHost(), redisUri.getPort()));
                }
                String masterName = getSentinelMasterName();
                JedisSentinelPool pool = new JedisSentinelPool(masterName, sentinels);
                return pool.getResource();
            }

            // Cluster
            Set<HostAndPort> jedisClusterNodes = new HashSet<>();
            for (RedisURI redisUri : uris) {
                jedisClusterNodes.add(new HostAndPort(redisUri.getHost(), redisUri.getPort()));
            }
            return new JedisCluster(jedisClusterNodes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private JedisCommands getJedisConnection() {
        if (jedisCommands == null)
            jedisCommands = createJedisConnection();
        return jedisCommands;
    }

    public static JedisCommands createJedisCommands() {
        return connectionManagement.getJedisConnection();
    }

    public static RedisStringAsyncCommands<String, String> createLettuceStringAsyncCommands() {
        return connectionManagement.getLettuceStringAsyncCommands();
    }

    public static RedisStringReactiveCommands<String, String> createLettuceStringReactiveCommands() {
        return connectionManagement.getLettuceStringReactiveCommands();
    }
}