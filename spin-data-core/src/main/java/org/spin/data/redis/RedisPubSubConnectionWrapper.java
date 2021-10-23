package org.spin.data.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.List;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/22</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RedisPubSubConnectionWrapper<K, V> implements AutoCloseable {

    private final RedisClientWrapper clientWrapper;

    private final StatefulRedisPubSubConnection<K, V> connection;

    private final StatefulRedisClusterPubSubConnection<K, V> clusterConnection;


    RedisPubSubConnectionWrapper(RedisClientWrapper clientWrapper, RedisCodec<K, V> codec) {
        this.clientWrapper = clientWrapper;
        if (clientWrapper.isCluster()) {
            connection = null;
            clusterConnection = clientWrapper.getClusterClient().connectPubSub(codec);
        } else {
            connection = clientWrapper.getClient().connectPubSub(codec);
            clusterConnection = null;
        }
    }

    public final V syncGet(K key) {
        return clientWrapper.isCluster() ?
            clusterConnection.sync().get(key) :
            connection.sync().get(key);
    }

    public final String syncSet(K key, V value) {
        return clientWrapper.isCluster() ?
            clusterConnection.sync().set(key, value) :
            connection.sync().set(key, value);
    }

    public final boolean syncSetNx(K key, V value, long timeoutInMillis) {
        String res = clientWrapper.isCluster() ?
            clusterConnection.sync().set(key, value, SetArgs.Builder.nx().px(timeoutInMillis)) :
            connection.sync().set(key, value, SetArgs.Builder.nx().px(timeoutInMillis));
        return "OK".equals(res);
    }

    public final Boolean syncSetNx(K key, V value) {
        return clientWrapper.isCluster() ?
            clusterConnection.sync().setnx(key, value) :
            connection.sync().setnx(key, value);
    }

    @SafeVarargs
    public final KeyValue<K, V> syncBlpop(long timeout, K... keys) {
        return clientWrapper.isCluster() ?
            clusterConnection.sync().blpop(timeout, keys) :
            connection.sync().blpop(timeout, keys);
    }

    @SafeVarargs
    public final <T> T syncEval(String script, ScriptOutputType type, K[] keys, V... values) {
        return clientWrapper.isCluster() ?
            clusterConnection.sync().eval(script, type, keys, values) :
            connection.sync().eval(script, type, keys, values);
    }

    @SafeVarargs
    public final void subscribe(K... channels) {
        if (clientWrapper.isCluster()) {
            clusterConnection.sync().subscribe(channels);
        } else {
            connection.sync().subscribe(channels);
        }
    }

    @SafeVarargs
    public final void unsubscribe(K... channels) {
        if (clientWrapper.isCluster()) {
            clusterConnection.sync().unsubscribe(channels);
        } else {
            connection.sync().unsubscribe(channels);
        }
    }

    @SafeVarargs
    public final void pSubscribe(K... patterns) {
        if (clientWrapper.isCluster()) {
            clusterConnection.sync().psubscribe(patterns);
        } else {
            connection.sync().psubscribe(patterns);
        }
    }

    @SafeVarargs
    public final void pUnsubscribe(K... patterns) {
        if (clientWrapper.isCluster()) {
            clusterConnection.sync().punsubscribe(patterns);
        } else {
            connection.sync().punsubscribe(patterns);
        }
    }


    public final List<K> pubsubChannels() {
        return clientWrapper.isCluster() ?
            clusterConnection.sync().pubsubChannels() :
            connection.sync().pubsubChannels();
    }

    public final List<K> pubsubChannels(K channel) {
        return clientWrapper.isCluster() ?
            clusterConnection.sync().pubsubChannels(channel) :
            connection.sync().pubsubChannels(channel);
    }

    /**
     * Add a new {@link RedisPubSubListener listener}.
     *
     * @param listener the listener, must not be {@code null}.
     */
    public final void addListener(RedisPubSubListener<K, V> listener) {
        if (clientWrapper.isCluster()) {
            clusterConnection.addListener(listener);
        } else {
            connection.addListener(listener);
        }
    }

    /**
     * Remove an existing {@link RedisPubSubListener listener}.
     *
     * @param listener the listener, must not be {@code null}.
     */
    public final void removeListener(RedisPubSubListener<K, V> listener) {
        if (clientWrapper.isCluster()) {
            clusterConnection.removeListener(listener);
        } else {
            connection.removeListener(listener);
        }
    }

    @Override
    public void close() {
        if (clientWrapper.isCluster()) {
            clusterConnection.close();
        } else {
            connection.close();
        }
    }
}
