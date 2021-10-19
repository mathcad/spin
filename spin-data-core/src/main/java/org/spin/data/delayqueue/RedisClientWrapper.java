package org.spin.data.delayqueue;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.spin.core.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

class RedisClientWrapper {
    private final RedisDelayQueueProperties delayQueueProperties;

    private RedisClient client;

    private RedisClusterClient clusterClient;

    private StatefulRedisConnection<String, String> connection;

    private StatefulRedisClusterConnection<String, String> clusterConnection;

    private final boolean isCluster;

    public RedisClientWrapper(RedisDelayQueueProperties queueProperties) {
        this.delayQueueProperties = queueProperties;
        if (null != queueProperties.getCluster() && CollectionUtils.isNotEmpty(queueProperties.getCluster().getNodes())) {
            this.isCluster = true;
            List<RedisURI> nodes = new LinkedList<>();
            for (String node : queueProperties.getCluster().getNodes()) {
                String[] ipAndPort = node.split(":");
                RedisURI uri = RedisURI.builder().withDatabase(queueProperties.getDatabase())
                    .withHost(ipAndPort[0])
                    .withPort(Integer.parseInt(ipAndPort[1]))
                    .withAuthentication(queueProperties.getUsername(), queueProperties.getPassword())
                    .withSsl(queueProperties.isSsl())
                    .withTimeout(queueProperties.getTimeout())
                    .withClientName(queueProperties.getClientName())
                    .build();
                nodes.add(uri);
            }
            clusterClient = RedisClusterClient.create(nodes);
            ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
            if (null != queueProperties.getConnectTimeout()) {
                builder.timeoutOptions(TimeoutOptions.builder().connectionTimeout().fixedTimeout(queueProperties.getConnectTimeout()).build());
            }
            builder.maxRedirects(queueProperties.getCluster().getMaxRedirects());
            clusterClient.setOptions(builder.build());
            clusterConnection = clusterClient.connect();
        } else {
            isCluster = false;
            RedisURI redisUri = RedisURI.builder().withDatabase(queueProperties.getDatabase())
                .withHost(queueProperties.getHost())
                .withPort(queueProperties.getPort())
                .withAuthentication(queueProperties.getUsername(), queueProperties.getPassword())
                .withSsl(queueProperties.isSsl())
                .withTimeout(queueProperties.getTimeout())
                .withClientName(queueProperties.getClientName())
                .build();
            client = RedisClient.create(redisUri);
            if (null != queueProperties.getConnectTimeout()) {
                client.setOptions(ClientOptions.builder()
                    .timeoutOptions(TimeoutOptions.builder().connectionTimeout().fixedTimeout(queueProperties.getConnectTimeout()).build())
                    .build());
            }
            connection = client.connect();
        }
    }


    public Long syncEval(String script, ScriptOutputType type, String[] keys, String... values) {
        if (isCluster) {
            return clusterConnection.sync().eval(script, type, keys, values);
        } else {
            return connection.sync().eval(script, type, keys, values);
        }
    }


    void shutdown() {
        if (isCluster) {
            clusterClient.shutdown();
        } else {
            client.shutdown();
        }
    }


    public StatefulConnection<String, String> newConnection() {
        if (isCluster) {
            return clusterClient.connect();
        } else {
            return client.connect();

        }
    }

    public RedisDelayQueueProperties getDelayQueueProperties() {
        return delayQueueProperties;
    }
}
