# redis-benchmark-java
Benchmarks of redis clients for JAVA lang

* *Redis clients performance comparison*: compares Jedis with Lettuce "ASYNC / REACTIVE" performance.

Requirements
* *Requires Java 10 to run*.

## how to run the benchmark

To run the benchmarks:

Copy the config.cfg to the target folder with the jar file. Modify it to point to the redis instance. Multiple redis instances are coma separated. 
* Redis Sentinel Cluster Connection: 
	- *redis-sentinel://192.168.1.104:6379,192.168.1.113:6379,192.168.1.124:6379*
* Redis Sharded Cluster Connection:
	- *redis://192.168.1.104:6379,192.168.1.113:6379,192.168.1.124:6379*
* Redis Standalone Connection: 
	- *redis://192.168.1.104:6379*
 
To test connectivity:
```bash
mvn clean install
java -jar target/benchmarks.jar -wi 1 -i 1 -t 1 -f 1
```

To run benchmark with 20 iterations to warmup "-wi 20", 20 iterations to measurements "-i 20", 10 threads "-t 10" and 10 forks "-f 10":
```bash
mvn clean install
java -jar target/benchmarks.jar -wi 20 -i 20 -t 10 -f 10
```

## sample benchmark results

Here is some sample benchmark results. It shows that Jedis client has more throughput compared with lettuce ASYNC and REACTIVE API's using 1 iterations to warmup, 1 thread and one fork. 

```bash
#Simply test:

java -jar target/benchmarks.jar -wi 1 -i 1 -t 1 -f 1

# Run complete. Total time: 00:02:12

Benchmark                                 Mode  Cnt   Score   Error   Units
RedisBenchmark.jedisSimpleGet            thrpt       10.565          ops/ms
RedisBenchmark.jedisSimpleSet            thrpt        7.911          ops/ms
RedisBenchmark.lettuceSimpleAsyncGet     thrpt        8.338          ops/ms
RedisBenchmark.lettuceSimpleAsyncSet     thrpt        7.510          ops/ms
RedisBenchmark.lettuceSimpleReactiveGet  thrpt        8.436          ops/ms
RedisBenchmark.lettuceSimpleReactiveSet  thrpt        7.328          ops/ms
RedisBenchmark.jedisSimpleGet             avgt        0.098           ms/op
RedisBenchmark.jedisSimpleSet             avgt        0.119           ms/op
RedisBenchmark.lettuceSimpleAsyncGet      avgt        0.120           ms/op
RedisBenchmark.lettuceSimpleAsyncSet      avgt        0.134           ms/op
RedisBenchmark.lettuceSimpleReactiveGet   avgt        0.123           ms/op
RedisBenchmark.lettuceSimpleReactiveSet   avgt        0.135           ms/op
```

## `jmh` command line options

```bash
$ java -jar target/benchmarks.jar -h

Usage: java -jar ... [regexp*] [options]
 [opt] means optional argument.
 <opt> means required argument.
 "+" means comma-separated list of values.
 "time" arguments accept time suffixes, like "100ms".

  [arguments]                 Benchmarks to run (regexp+).

  -bm <mode>                  Benchmark mode. Available modes are: [Throughput/thrpt,
                              AverageTime/avgt, SampleTime/sample, SingleShotTime/ss,
                              All/all]

  -bs <int>                   Batch size: number of benchmark method calls per
                              operation. Some benchmark modes may ignore this
                              setting, please check this separately.

  -e <regexp+>                Benchmarks to exclude from the run.

  -f <int>                    How many times to fork a single benchmark. Use 0 to
                              disable forking altogether. Warning: disabling
                              forking may have detrimental impact on benchmark
                              and infrastructure reliability, you might want
                              to use different warmup mode instead.

  -foe <bool>                 Should JMH fail immediately if any benchmark had
                              experienced an unrecoverable error? This helps
                              to make quick sanity tests for benchmark suites,
                              as well as make the automated runs with checking error
                              codes.

  -gc <bool>                  Should JMH force GC between iterations? Forcing
                              the GC may help to lower the noise in GC-heavy benchmarks,
                              at the expense of jeopardizing GC ergonomics decisions.
                              Use with care.

  -h                          Display help.

  -i <int>                    Number of measurement iterations to do. Measurement
                              iterations are counted towards the benchmark score.

  -jvm <string>               Use given JVM for runs. This option only affects forked
                              runs.

  -jvmArgs <string>           Use given JVM arguments. Most options are inherited
                              from the host VM options, but in some cases you want
                              to pass the options only to a forked VM. Either single
                              space-separated option line, or multiple options
                              are accepted. This option only affects forked runs.

  -jvmArgsAppend <string>     Same as jvmArgs, but append these options before
                              the already given JVM args.

  -jvmArgsPrepend <string>    Same as jvmArgs, but prepend these options before
                              the already given JVM arg.

  -l                          List the benchmarks that match a filter, and exit.

  -lp                         List the benchmarks that match a filter, along with
                              parameters, and exit.

  -lprof                      List profilers.

  -lrf                        List machine-readable result formats.

  -o <filename>               Redirect human-readable output to a given file.

  -opi <int>                  Override operations per invocation, see @OperationsPerInvocation
                              Javadoc for details.

  -p <param={v,}*>            Benchmark parameters. This option is expected to
                              be used once per parameter. Parameter name and parameter
                              values should be separated with equals sign. Parameter
                              values should be separated with commas.

  -prof <profiler>            Use profilers to collect additional benchmark data.
                              Some profilers are not available on all JVMs and/or
                              all OSes. Please see the list of available profilers
                              with -lprof.

  -r <time>                   Minimum time to spend at each measurement iteration.
                              Benchmarks may generally run longer than iteration
                              duration.

  -rf <type>                  Format type for machine-readable results. These
                              results are written to a separate file (see -rff).
                              See the list of available result formats with -lrf.

  -rff <filename>             Write machine-readable results to a given file.
                              The file format is controlled by -rf option. Please
                              see the list of result formats for available formats.

  -si <bool>                  Should JMH synchronize iterations? This would significantly
                              lower the noise in multithreaded tests, by making
                              sure the measured part happens only when all workers
                              are running.

  -t <int>                    Number of worker threads to run with. 'max' means
                              the maximum number of hardware threads available
                              on the machine, figured out by JMH itself.

  -tg <int+>                  Override thread group distribution for asymmetric
                              benchmarks. This option expects a comma-separated
                              list of thread counts within the group. See @Group/@GroupThreads
                              Javadoc for more information.

  -to <time>                  Timeout for benchmark iteration. After reaching
                              this timeout, JMH will try to interrupt the running
                              tasks. Non-cooperating benchmarks may ignore this
                              timeout.

  -tu <TU>                    Override time unit in benchmark results. Available
                              time units are: [m, s, ms, us, ns].

  -v <mode>                   Verbosity mode. Available modes are: [SILENT, NORMAL,
                              EXTRA]

  -w <time>                   Minimum time to spend at each warmup iteration. Benchmarks
                              may generally run longer than iteration duration.

  -wbs <int>                  Warmup batch size: number of benchmark method calls
                              per operation. Some benchmark modes may ignore this
                              setting.

  -wf <int>                   How many warmup forks to make for a single benchmark.
                              All iterations within the warmup fork are not counted
                              towards the benchmark score. Use 0 to disable warmup
                              forks.

  -wi <int>                   Number of warmup iterations to do. Warmup iterations
                              are not counted towards the benchmark score.

  -wm <mode>                  Warmup mode for warming up selected benchmarks.
                              Warmup modes are: INDI = Warmup each benchmark individually,
                              then measure it. BULK = Warmup all benchmarks first,
                              then do all the measurements. BULK_INDI = Warmup
                              all benchmarks first, then re-warmup each benchmark
                              individually, then measure it.

  -wmb <regexp+>              Warmup benchmarks to include in the run in addition
                              to already selected by the primary filters. Harness
                              will not measure these benchmarks, but only use them
                              for the warmup.
```

# references
   1. [jmh official site](http://openjdk.java.net/projects/code-tools/jmh/)
   1. [jmh sample benchmarks](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)
   1. [Introduction to JMH by Mikhail Vorontsov (java-performance.info)](http://java-performance.info/jmh/)   
