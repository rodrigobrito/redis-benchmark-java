# redis-benchmark-java
Benchmarks of redis clients for JAVA lang

* *Redis clients performance comparison*: compares Jedis with Lettuce "ASYNC / REACTIVE" performance.

Requirements
* *Requires Java 10 to run*.

## how to run the benchmark

To run the benchmarks:

Copy config.cfg to the target folder with the jar file. Modify it to point to the redis instance. Multiple redis instances are coma separated.
 
* Redis Sentinel Cluster Connection: 
	- *redis-sentinel://192.168.1.104:26379,192.168.1.113:26379,192.168.1.124:26379*
	- *Update redis.sentinel.master.name from config.cfg to sentinel group-name configured in redis-sentinel.conf*
	    - *Sample: redis.sentinel.master.name=mymaster*
* Redis Sharded Cluster Connection:
	- *redis://192.168.1.104:6379,192.168.1.113:6379,192.168.1.124:6379*
* Redis Standalone Connection: 
	- *redis://192.168.1.104:6379*
	
You can use your application payload to run this benchmark, you can also configure the amount of keys you want to have available to execute **GET** benchmarks. To do this just change the attributes *benchmark.key.amount* and *benchmark.key.data*.
 
Parameter options:

`-wi 20` *Warm-up*  
* 20 warm-up cycles (Without measurement, providing the opportunity to the JVM to optimize the code before the measurement starts).

`-i 20` *Measurements iterations* 
* 20 real measurement iterations for every test.

`-t 10` *Threads*

* Amount of threads to run benchmark.

`-f 10` *Forks*
* Separate execution environments.

To build:
```bash
$ git clone https://github.com/rodrigobrito/redis-benchmark-java.git
$ cd redis-benchmark-java
$ mvn clean install
```

Test connectivity and create keys to execute the **GET** benchmarks:
```bash
$ cp config.cfg ./target/
$ java -jar target/benchmarks.jar -wi 1 -i 1 -t 1 -f 1
```

To run benchmark with 20 iterations to warmup "-wi 20", 20 iterations to measurements "-i 20", 100 threads "-t 100" and 3 forks "-f 3":
```bash
$ java -jar target/benchmarks.jar -wi 20 -i 20 -t 100 -f 3
```

## benchmark results

###### REDIS SENTINEL 

Cloud: Huawei
* 3 nodes of 4 vCPUs, 32 GB and CentOS Linux release 7.7.1908.         
    - One master and two replicas (Two processes per node: Redis and Sentinel)
* 1 node of 4 vCPUs, 16 GB and CentOS Linux release 7.7.1908
    - Java client benchmark 

Benchmark Configuration
* Amount of keys: 1MM
* Key data size: 5 KB
* Test benchmark with 1 warm-up, 1 measurement iteration, 1 thread and 1 fork.
     - It shows that Jedis client has more throughput compared with lettuce ASYNC and REACTIVE API's to **GET** data.
     - It shows that Lettuce reactive API has more throughput compared with Jedis to **SET** data.
 * Productive benchmark with 20 warm-up, 20 measurements iterations, 100 threads and 3 forks.
    - Coming soon...

```bash
# Test:

# Best throughput (thrpt):
# GET - Jedis simple get: 5.710 operations per millisecond or 5,710 operations per second.
# SET - Lettuce simple reactive set: 8.928 operations per millisecond or 8,928 operations per second.

# Best average time (avgt):
# GET - Jedis simple get: 0.156 millisecond (1 and a half microseconds) per operation.
# SET - Lettuce simple reactive set: 0.110 millisecond (1 microsecond) per operation.

$ java -jar target/benchmarks.jar -wi 1 -i 1 -t 1 -f 1 

# Run complete. Total time: 00:02:27

Benchmark                                 Mode  Cnt  Score   Error   Units
RedisBenchmark.jedisSimpleGet            thrpt       5.710          ops/ms
RedisBenchmark.jedisSimpleSet            thrpt       4.494          ops/ms
RedisBenchmark.lettuceSimpleAsyncGet     thrpt       3.680          ops/ms
RedisBenchmark.lettuceSimpleAsyncSet     thrpt       8.923          ops/ms
RedisBenchmark.lettuceSimpleReactiveGet  thrpt       4.488          ops/ms
RedisBenchmark.lettuceSimpleReactiveSet  thrpt       8.928          ops/ms
RedisBenchmark.jedisSimpleGet             avgt       0.156           ms/op
RedisBenchmark.jedisSimpleSet             avgt       0.119           ms/op
RedisBenchmark.lettuceSimpleAsyncGet      avgt       0.225           ms/op
RedisBenchmark.lettuceSimpleAsyncSet      avgt       0.112           ms/op
RedisBenchmark.lettuceSimpleReactiveGet   avgt       0.235           ms/op
RedisBenchmark.lettuceSimpleReactiveSet   avgt       0.110           ms/op

# Productive benchmark:

$ java -jar target/benchmarks.jar -wi 20 -i 20 -t 100 -f 3

# Run complete. Total time: 02:11:18

Benchmark                                 Mode  Cnt    Score   Error   Units
RedisBenchmark.jedisSimpleGet            thrpt   60   17.858 ± 1.022  ops/ms
RedisBenchmark.jedisSimpleSet            thrpt   60  143.064 ± 2.235  ops/ms
RedisBenchmark.lettuceSimpleAsyncGet     thrpt   60   12.284 ± 0.151  ops/ms
RedisBenchmark.lettuceSimpleAsyncSet     thrpt   60  131.441 ± 0.484  ops/ms
RedisBenchmark.lettuceSimpleReactiveGet  thrpt   60   12.299 ± 0.060  ops/ms
RedisBenchmark.lettuceSimpleReactiveSet  thrpt   60  122.818 ± 1.385  ops/ms
RedisBenchmark.jedisSimpleGet             avgt   40    7.587 ± 1.216   ms/op
RedisBenchmark.jedisSimpleSet             avgt   60    0.704 ± 0.010   ms/op
RedisBenchmark.lettuceSimpleAsyncGet      avgt   60    8.037 ± 0.056   ms/op
RedisBenchmark.lettuceSimpleAsyncSet      avgt   60    0.770 ± 0.012   ms/op
RedisBenchmark.lettuceSimpleReactiveGet   avgt   60    8.063 ± 0.039   ms/op
RedisBenchmark.lettuceSimpleReactiveSet   avgt   60    0.806 ± 0.005   ms/op
```

`Throughput`
<br/>
<img src="https://raw.githubusercontent.com/rodrigobrito/redis-benchmark-java/master/img/BenchmarkRedisSentinelThroughput-wi20-i20-t100-f3.png"/>
<br/>

###### REDIS CLUSTER

Cloud: Huawei
* 6 nodes of 4 vCPUs, 32 GB and CentOS Linux release 7.7.1908.         
    - Three masters and three replicas (One processes per node)
* 1 node of 4 vCPUs, 16 GB and CentOS Linux release 7.7.1908
    - Java client benchmark 
    
Benchmark Configuration
* Amount of keys: 1MM
* Key data size: 5 KB
* Productive benchmark with 20 warm-up, 20 measurements iterations, 100 threads and 3 forks.
  - It shows that Lettuce reactive API has more throughput compared with Jedis to **GET** data.
  - It shows that Lettuce async API has more throughput compared with Jedis to **SET** data.

```bash
$ java -jar target/benchmarks.jar -wi 20 -i 20 -t 100 -f 3 

# Best throughput (thrpt):
# GET - Lettuce simple reactive get: 19.598 operations per millisecond or 19,598 operations per second.
# SET - Lettuce simple async set: 220.132 operations per millisecond or 220,132 operations per second.

# Run complete. Total time: 02:24:08

Benchmark                                 Mode  Cnt    Score   Error   Units
RedisBenchmark.jedisSimpleGet            thrpt   60   18.842 ± 1.491  ops/ms
RedisBenchmark.jedisSimpleSet            thrpt   60  150.455 ± 5.209  ops/ms
RedisBenchmark.lettuceSimpleAsyncGet     thrpt   60   19.377 ± 0.860  ops/ms
RedisBenchmark.lettuceSimpleAsyncSet     thrpt   60  220.132 ± 3.128  ops/ms
RedisBenchmark.lettuceSimpleReactiveGet  thrpt   60   19.598 ± 0.976  ops/ms
RedisBenchmark.lettuceSimpleReactiveSet  thrpt   60  207.576 ± 2.282  ops/ms
RedisBenchmark.jedisSimpleGet             avgt   60    8.164 ± 2.166   ms/op
RedisBenchmark.jedisSimpleSet             avgt   60    0.668 ± 0.024   ms/op
RedisBenchmark.lettuceSimpleAsyncGet      avgt   60    5.245 ± 0.271   ms/op
RedisBenchmark.lettuceSimpleAsyncSet      avgt   60    0.462 ± 0.008   ms/op
RedisBenchmark.lettuceSimpleReactiveGet   avgt   60    5.266 ± 0.240   ms/op
RedisBenchmark.lettuceSimpleReactiveSet   avgt   60    0.485 ± 0.009   ms/op
```

`Throughput`
<br/>
<img src="https://raw.githubusercontent.com/rodrigobrito/redis-benchmark-java/master/img/BenchmarkRedisClustertThroughput-wi20-i20-t100-f3.png"/>
<br/>

## remark
###### Jedis
In each benchmark method we have a `try-catch` so that in a failover scenario a new master or slave can be elected.
* `JedisConnectionManagement.getCommands()` call `jedisPool.getResource()` to resolve a new master/slave in a **Sentinel Cluster**.
###### Lettuce
* Coming soon

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
   1. [Avoiding Benchmarking Pitfalls on the JVM](https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html)
