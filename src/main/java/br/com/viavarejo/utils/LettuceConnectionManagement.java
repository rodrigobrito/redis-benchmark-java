package br.com.viavarejo.utils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.masterslave.StatefulRedisMasterSlaveConnection;

import java.util.List;

public final class LettuceConnectionManagement {
    private enum ConnectionType {
        Standalone,
        Sentinel,
        Cluster
    }

    private static LettuceConnectionManagement connectionManagement;

    private StatefulRedisClusterConnection<String, String> cluster;
    private StatefulRedisMasterSlaveConnection<String, String> sentinel;
    private StatefulRedisConnection<String, String> standalone;
    private ConnectionType connectionType = ConnectionType.Standalone;

    private LettuceConnectionManagement() {
    }

    private StatefulConnection<String, String> createLettuceConnection() {
        try {
            List<RedisURI> uris = BenchmarkConfiguration.get().getRedisUris();
            Boolean isSentinel = BenchmarkConfiguration.get().isSentinel();
            // Standalone
            if (uris.size() == 1) {
                RedisClient client = RedisClient.create(uris.get(0));
                return client.connect();
            }
            // Sentinel
            if (isSentinel) {
                RedisURI firstUri = uris.get(0);
                String sentinelMasterName = BenchmarkConfiguration.get().getSentinelMasterName();
                RedisURI.Builder builder = RedisURI.Builder.sentinel(firstUri.getHost(), firstUri.getPort(), sentinelMasterName);
                for (int i = 1; i < uris.size(); i++) {
                    RedisURI currentUri = uris.get(i);
                    builder = builder.withSentinel(currentUri.getHost(), currentUri.getPort());
                }
                RedisURI finalUri = builder.build();
                RedisClient redisClient = RedisClient.create(finalUri);
                return redisClient.connect();
            }
            // Cluster
            RedisClusterClient clusterClient = RedisClusterClient.create(uris);
            ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                    .enablePeriodicRefresh()
                    .build();

            clusterClient.setOptions(ClusterClientOptions.builder()
                    .topologyRefreshOptions(topologyRefreshOptions)
                    .build());
            return clusterClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public RedisStringAsyncCommands<String, String> async() {
        RedisStringAsyncCommands<String, String> commands = null;
        switch (connectionType) {
            case Cluster:
                commands = cluster.async();
                break;
            case Sentinel:
                commands = sentinel.async();
                break;
            case Standalone:
                commands = standalone.async();
                break;
        }
        return commands;
    }

    public RedisStringReactiveCommands<String, String> reactive() {
        RedisStringReactiveCommands<String, String> commands = null;
        switch (connectionType) {
            case Cluster:
                commands = cluster.reactive();
                break;
            case Sentinel:
                commands = sentinel.reactive();
                break;
            case Standalone:
                commands = standalone.reactive();
                break;
        }
        return commands;
    }

    private void createConnection() {
        StatefulConnection<String, String> conn = createLettuceConnection();
        if (conn instanceof StatefulRedisClusterConnection) {
            cluster = ((StatefulRedisClusterConnection<String, String>) conn);
            connectionType = ConnectionType.Cluster;
            return;
        }
        if (conn instanceof StatefulRedisMasterSlaveConnection) {
            sentinel = ((StatefulRedisMasterSlaveConnection<String, String>) conn);
            connectionType = ConnectionType.Sentinel;
            return;
        }
        standalone = ((StatefulRedisConnection<String, String>) conn);
        connectionType = connectionType.Standalone;
    }

    public static LettuceConnectionManagement get() {
        if (connectionManagement == null) {
            connectionManagement = new LettuceConnectionManagement();
            connectionManagement.createConnection();
        }
        return connectionManagement;
    }
}
