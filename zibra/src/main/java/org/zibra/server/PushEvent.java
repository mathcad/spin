package org.zibra.server;

public interface PushEvent {
    void subscribe(String topic, String id, ZibraService service);

    void unsubscribe(String topic, String id, ZibraService service);
}
