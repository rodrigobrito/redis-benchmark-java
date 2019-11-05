package br.com.viavarejo.utils;

import io.lettuce.core.RedisURI;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JedisConnectionManagement {
    private static final JedisConnectionManagement connectionManagement = new JedisConnectionManagement();
    private static Boolean connectionCreated = false;

    private Jedis jedisStandalone;
    private JedisSentinelPool jedisSentinelPool;
    private JedisCluster jedisCluster;
    private ConnectionType connectionType = ConnectionType.Standalone;

    private JedisConnectionManagement() {
    }

    private void createJedisConnection() {
        try {
            List<RedisURI> uris = BenchmarkConfiguration.get().getRedisUris();
            // Standalone
            if (uris.size() == 1) {
                connectionType = ConnectionType.Standalone;
                RedisURI redisUri = uris.get(0);
                String redisHost = redisUri.getHost();
                int redisPort = redisUri.getPort();
                jedisStandalone = new Jedis(redisHost, redisPort);
                return;
            }
            // Sentinel
            if (BenchmarkConfiguration.get().isSentinel()) {
                connectionType = ConnectionType.Sentinel;
                Set sentinels = new HashSet();
                for (RedisURI redisUri : uris) {
                    sentinels.add(String.format("%s:%s", redisUri.getHost(), redisUri.getPort()));
                }
                String masterName = BenchmarkConfiguration.get().getSentinelMasterName();
                GenericObjectPoolConfig config = new GenericObjectPoolConfig();
                config.setMaxTotal(2);
                config.setBlockWhenExhausted(false);
                JedisSentinelPool pool = new JedisSentinelPool(masterName, sentinels, config,2000);
                jedisSentinelPool = pool;
                return;
            }
            // Cluster
            connectionType = ConnectionType.Cluster;
            Set<HostAndPort> jedisClusterNodes = new HashSet<>();
            for (RedisURI redisUri : uris) {
                jedisClusterNodes.add(new HostAndPort(redisUri.getHost(), redisUri.getPort()));
            }
            jedisCluster = new JedisCluster(jedisClusterNodes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private JedisCommands getJedisCommands() {
        JedisCommands commands = null;
        switch (connectionType) {
            case Standalone:
                commands = jedisStandalone;
                break;
            case Sentinel:
                commands = jedisSentinelPool.getResource();
                break;
            case Cluster:
                commands = jedisCluster;
                break;
        }
        return commands;
    }

    public static JedisCommands getCommands() {
        if (!connectionCreated)
            connectionManagement.createJedisConnection();
        return connectionManagement.getJedisCommands();
    }
}