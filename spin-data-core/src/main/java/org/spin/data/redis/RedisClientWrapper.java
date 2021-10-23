package org.spin.data.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.spin.core.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

public class RedisClientWrapper implements AutoCloseable {
    private final LettuceRedisProperties lettuceRedisProperties;

    private RedisClient client;

    private RedisClusterClient clusterClient;

    private final boolean isCluster;

    public RedisClientWrapper(LettuceRedisProperties queueProperties) {
        this.lettuceRedisProperties = queueProperties;
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
        }
    }


    @Override
    public void close() {
        if (isCluster) {
            clusterClient.shutdown();
        } else {
            client.shutdown();
        }
    }

    public RedisConnectionWrapper<String, String> connect() {
        return new RedisConnectionWrapper<>(this, StringCodec.UTF8);
    }

    public <K, V> RedisConnectionWrapper<K, V> connect(RedisCodec<K, V> codec) {
        return new RedisConnectionWrapper<>(this, codec);
    }
    
    public RedisPubSubConnectionWrapper<String, String> connectPubSub() {
        return new RedisPubSubConnectionWrapper<>(this, StringCodec.UTF8);
    }

    public <K, V> RedisPubSubConnectionWrapper<K, V> connectPubSub(RedisCodec<K, V> codec) {
        return new RedisPubSubConnectionWrapper<>(this, codec);
    }

    public RedisClient getClient() {
        return client;
    }

    public RedisClusterClient getClusterClient() {
        return clusterClient;
    }

    public boolean isCluster() {
        return isCluster;
    }

    public LettuceRedisProperties getLettuceRedisProperties() {
        return lettuceRedisProperties;
    }
}
