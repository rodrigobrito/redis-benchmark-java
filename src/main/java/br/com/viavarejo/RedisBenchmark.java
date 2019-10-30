package br.com.viavarejo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Mono;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 10)
@Threads(1)
@State(Scope.Thread)
@Measurement(iterations = 20, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class RedisBenchmark {
    private JedisCommands jedis;
    private StatefulRedisConnection<String, String> lettuceConnection;

    private static Integer jedisSetCount = 0;
    private static Integer lettuceAsyncSetCount = 0;
    private static Integer lettuceReactSetCount = 0;

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

    @Setup
    public void setup() {
        Properties properties = getProperties();
        String redisConnection = properties.getProperty("redis.connections");

        String[] nodes = redisConnection.split("/");

        // Redis URI
        RedisURI redisUri = RedisURI.create(redisConnection);

        String redisHost = redisUri.getHost();
        int redisPort = redisUri.getPort();

        // Jedis connection
        jedis = new Jedis(redisHost, redisPort);
        jedis.set("a", "any value");

        // Lettuce connection
        RedisClient client = RedisClient.create(redisUri);
        lettuceConnection = client.connect();
    }

    @Benchmark
    public String jedisSimpleGet() {
        return jedis.get("a");
    }

    @Benchmark
    public void jedisSimpleSet() {
        jedisSetCount++;
        jedis.set(String.format("JedisTest%s", jedisSetCount), jedisSetCount.toString());
    }

    @Benchmark
    public String lettuceSimpleAsyncGet() {
        String result = null;
        RedisStringAsyncCommands<String, String> async = lettuceConnection.async();
        RedisFuture<String> future = async.get("a");
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceSimpleAsyncSet() {
        String result = null;
        lettuceAsyncSetCount++;
        RedisStringAsyncCommands<String, String> async = lettuceConnection.async();
        RedisFuture<String> future = async.set(String.format("lettuceSetAsync%s", lettuceAsyncSetCount), lettuceAsyncSetCount.toString());
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceSimpleReactiveGet() {
        String result = null;
        RedisStringReactiveCommands<String, String> reactive = lettuceConnection.reactive();
        Mono<String> future = reactive.get("a");
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceSimpleReactiveSet() {
        String result = null;
        lettuceReactSetCount++;
        RedisStringReactiveCommands<String, String> reactive = lettuceConnection.reactive();
        Mono<String> future = reactive.set(String.format("lettuceSetReactive%s", lettuceReactSetCount), lettuceReactSetCount.toString());
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}