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
@OutputTimeUnit(TimeUnit.SECONDS)
public class RedisBenchmark {
    private JedisCommands jedis;
    private LettuceConnectionManagement lettuce;

    private static Integer jedisGetCount = 0;
    private static Integer jedisSetCount = 0;

    private static Integer lettuceAsyncGetCount = 0;
    private static Integer lettuceAsyncSetCount = 0;

    private static Integer lettuceReactiveGetCount = 0;
    private static Integer lettuceReactiveSetCount = 0;

    @Setup
    public void setup() {
        Util util = Util.create();
        util.createThreeMillionOfKeys();

        jedis = JedisConnectionManagement.get();
        lettuce = LettuceConnectionManagement.get();
    }

    @Benchmark
    public String jedisSimpleGet() {
        if (jedisGetCount >= Util.ThreeMillion) {
            jedisGetCount = 0;
        }
        jedisGetCount++;
        return jedis.get(String.format(Util.KeyPrefix, jedisGetCount));
    }

    @Benchmark
    public void jedisSimpleSet() {
        jedisSetCount++;
        jedis.set(String.format("JedisSetTest%s", jedisSetCount), jedisSetCount.toString());
    }

    @Benchmark
    public String lettuceSimpleAsyncGet() {
        if (lettuceAsyncGetCount >= Util.ThreeMillion) {
            lettuceAsyncGetCount = 0;
        }
        lettuceAsyncGetCount++;
        RedisStringAsyncCommands<String, String> async = lettuce.async();
        RedisFuture<String> future = async.get(String.format(Util.KeyPrefix, lettuceAsyncGetCount));
        String result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceSimpleAsyncSet() {
        lettuceAsyncSetCount++;
        RedisStringAsyncCommands<String, String> async = lettuce.async();
        RedisFuture<String> future = async.set(String.format("LettuceSetAsync%s", lettuceAsyncSetCount), lettuceAsyncSetCount.toString());
        String result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceSimpleReactiveGet() {
        if (lettuceReactiveGetCount >= Util.ThreeMillion) {
            lettuceReactiveGetCount = 0;
        }
        lettuceReactiveGetCount++;
        RedisStringReactiveCommands<String, String> reactive = lettuce.reactive();
        Mono<String> future = reactive.get(String.format(Util.KeyPrefix, lettuceReactiveGetCount));
        String result = null;
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceSimpleReactiveSet() {
        lettuceReactiveSetCount++;
        RedisStringReactiveCommands<String, String> reactive = lettuce.reactive();
        Mono<String> future = reactive.set(String.format("lettuceSetReactive%s", lettuceReactiveSetCount), lettuceReactiveSetCount.toString());
        String result = null;
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}