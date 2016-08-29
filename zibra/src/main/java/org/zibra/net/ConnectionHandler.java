package org.zibra.net;

import java.nio.ByteBuffer;

public interface ConnectionHandler {
    void onConnect(Connection conn);

    void onConnected(Connection conn);

    void onReceived(Connection conn, ByteBuffer data, Integer id);

    void onSended(Connection conn, Integer id);

    void onClose(Connection conn);

    void onError(Connection conn, Exception e);

    void onTimeout(Connection conn, TimeoutType type);

    int getReadTimeout();

    int getWriteTimeout();

    int getConnectTimeout();
}
