package br.com.viavarejo.utils;

import io.lettuce.core.RedisURI;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JedisConnectionManagement {
    private static final JedisConnectionManagement connectionManagement = new JedisConnectionManagement();
    private JedisCommands jedisCommands;

    private JedisConnectionManagement() {
    }

    private JedisCommands createJedisConnection() {
        try {
            List<RedisURI> uris = BenchmarkConfiguration.get().getRedisUris();
            // Standalone
            if (uris.size() == 1) {
                RedisURI redisUri = uris.get(0);
                String redisHost = redisUri.getHost();
                int redisPort = redisUri.getPort();
                return new Jedis(redisHost, redisPort);
            }
            // Sentinel
            if (BenchmarkConfiguration.get().isSentinel()) {
                Set sentinels = new HashSet();
                for (RedisURI redisUri : uris) {
                    sentinels.add(String.format("%s:%s", redisUri.getHost(), redisUri.getPort()));
                }
                String masterName = BenchmarkConfiguration.get().getSentinelMasterName();
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

    public static JedisCommands get() {
        return connectionManagement.getJedisConnection();
    }
}