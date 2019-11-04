package br.com.viavarejo;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Program {
    public static void main(String[] args) throws RunnerException {
        System.out.println("Starting benchmark...");

        Options options = new OptionsBuilder()
                .include(RedisBenchmark.class.getSimpleName())
                .output("redis-throughput.log")
                .forks(0)
                .build();
        new Runner(options).run();
    }
}