package org.zibra.server;

import org.zibra.util.concurrent.Action;
import org.zibra.util.concurrent.Promise;

public interface ZibraClients {
    String[] idlist(String topic);

    boolean exist(String topic, String id);

    void broadcast(String topic, Object result);

    void broadcast(String topic, Object result, Action<String[]> callback);

    void multicast(String topic, String[] ids, Object result);

    void multicast(String topic, String[] ids, Object result, Action<String[]> callback);

    void unicast(String topic, String id, Object result);

    void unicast(String topic, String id, Object result, Action<Boolean> callback);

    Promise<String[]> push(String topic, Object result);

    Promise<String[]> push(String topic, String[] ids, Object result);

    Promise<Boolean> push(String topic, String id, Object result);
}
