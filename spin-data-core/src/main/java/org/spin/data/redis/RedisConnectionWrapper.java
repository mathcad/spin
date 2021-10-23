package org.spin.data.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/22</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RedisConnectionWrapper<K, V> implements AutoCloseable {

    private final RedisClientWrapper clientWrapper;

    private final StatefulRedisConnection<K, V> connection;

    private final StatefulRedisClusterConnection<K, V> clusterConnection;


    RedisConnectionWrapper(RedisClientWrapper clientWrapper, RedisCodec<K, V> codec) {
        this.clientWrapper = clientWrapper;
        if (clientWrapper.isCluster()) {
            connection = null;
            clusterConnection = clientWrapper.getClusterClient().connect(codec);
        } else {
            connection = clientWrapper.getClient().connect(codec);
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

    public final Long publish(K channel, V message) {
        return clientWrapper.isCluster() ?
            clusterConnection.sync().publish(channel, message) :
            connection.sync().publish(channel, message);
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
