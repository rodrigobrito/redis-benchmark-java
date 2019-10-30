package br.com.viavarejo;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisCommands;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 1)
@Threads(1)
@State(Scope.Thread)
@Measurement(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class RedisBenchmark {
    private JedisCommands jedis;

    private static Integer jedisSetCount = 0;
    private static Integer lettuceAsyncSetCount = 0;
    private static Integer lettuceReactSetCount = 0;

    @Setup
    public void setup() {
        jedis = RedisConnectionManagement.createJedisCommands();
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
        RedisStringAsyncCommands<String, String> async = RedisConnectionManagement.createLettuceStringAsyncCommands();
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
        RedisStringAsyncCommands<String, String> async = RedisConnectionManagement.createLettuceStringAsyncCommands();
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
        RedisStringReactiveCommands<String, String> reactive = RedisConnectionManagement.createLettuceStringReactiveCommands();
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
        RedisStringReactiveCommands<String, String> reactive = RedisConnectionManagement.createLettuceStringReactiveCommands();
        Mono<String> future = reactive.set(String.format("lettuceSetReactive%s", lettuceReactSetCount), lettuceReactSetCount.toString());
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}