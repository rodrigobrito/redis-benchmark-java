package br.com.viavarejo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class RedisConnectionManagement {
    private static final RedisConnectionManagement connectionManagement = new RedisConnectionManagement();
    private Boolean lettuceCluster = false;
    private StatefulConnection lettuceConnection;
    private JedisCommands jedisCommands;

    private RedisConnectionManagement() {
    }

    private Properties getProperties() {
        Properties prop = new Properties();
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("application.properties")) {
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    private String getConnectionString() {
        Properties properties = getProperties();
        return properties.getProperty("redis.connections");
    }

    private List<RedisURI> getRedisUris() {
        String redisConnection = getConnectionString();
        List<RedisURI> uris = new ArrayList<>();
        String[] nodes = redisConnection.split("/");
        for (String node : nodes) {
            String[] hostAndPort = node.split(":");
            String host = hostAndPort[0];
            String port = hostAndPort[1];
            RedisURI uri = RedisURI.create(host, Integer.parseInt(port));
            uris.add(uri);
        }
        return uris;
    }

    private StatefulConnection createLettuceConnection() {
        List<RedisURI> uris = getRedisUris();

        if (uris.size() == 1) {
            RedisClient client = RedisClient.create(uris.get(0));
            lettuceCluster = false;
            return client.connect();
        }

        lettuceCluster = true;

        RedisClusterClient clusterClient = RedisClusterClient.create(uris);
        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh()
                .build();
        clusterClient.setOptions(ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .build());

        return clusterClient.connect();
    }

    private RedisStringAsyncCommands<String, String> getLettuceStringAsyncCommands() {
        if (lettuceConnection == null) {
            lettuceConnection = createLettuceConnection();
        }
        if (lettuceCluster) {
            StatefulRedisClusterConnection<String, String> cluster = ((StatefulRedisClusterConnection<String, String>) lettuceConnection);
            return cluster.async();
        }
        StatefulRedisConnection<String, String> defaultConnection = ((StatefulRedisConnection<String, String>) lettuceConnection);
        return defaultConnection.async();
    }

    private JedisCommands getJedisConnection() {
        List<RedisURI> uris = getRedisUris();
        if (uris.size() == 1) {
            RedisURI redisUri = uris.get(0);
            String redisHost = redisUri.getHost();
            int redisPort = redisUri.getPort();
            return new Jedis(redisHost, redisPort);
        }
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        for (RedisURI redisUri : uris) {
            jedisClusterNodes.add(new HostAndPort(redisUri.getHost(), redisUri.getPort()));
        }
        return new JedisCluster(jedisClusterNodes);
    }

    public JedisCommands getJedisCommands() {
        if (jedisCommands == null)
            jedisCommands = getJedisConnection();
        return jedisCommands;
    }

    public static JedisCommands createJedisCommands() {
        return connectionManagement.getJedisCommands();
    }

    public static RedisStringAsyncCommands<String, String> createLettuceStringAsyncCommands() {
        return connectionManagement.getLettuceStringAsyncCommands();
    }
}