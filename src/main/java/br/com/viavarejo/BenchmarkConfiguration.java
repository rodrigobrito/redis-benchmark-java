package br.com.viavarejo;

import io.lettuce.core.RedisURI;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class BenchmarkConfiguration {
    private static final BenchmarkConfiguration configuration = new BenchmarkConfiguration();

    private BenchmarkConfiguration() {
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

    public String getConnectionString() {
        Properties properties = getProperties();
        return properties.getProperty("redis.connection");
    }

    public String getSentinelMasterName() {
        Properties properties = getProperties();
        return properties.getProperty("redis.sentinel.master.group.name");
    }

    public boolean isSentinel() {
        String redisConnection = getConnectionString();
        return redisConnection.contains("sentinel");
    }

    public List<RedisURI> getRedisUris() {
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

    public static BenchmarkConfiguration get() {
        return configuration;
    }
}
