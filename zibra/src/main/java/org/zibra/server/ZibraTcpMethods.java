package org.zibra.server;

import org.zibra.common.HproseMethods;
import org.zibra.common.ZibraContext;

import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class ZibraTcpMethods extends HproseMethods {

    @Override
    protected int getCount(Type[] paramTypes) {
        int i = paramTypes.length;
        if ((i > 0) && (paramTypes[i - 1] instanceof Class<?>)) {
            Class<?> paramType = (Class<?>) paramTypes[i - 1];
            if (paramType.equals(ZibraContext.class) ||
                    paramType.equals(ServiceContext.class) ||
                    paramType.equals(TcpContext.class) ||
                    paramType.equals(SocketChannel.class) ||
                    paramType.equals(Socket.class)) {
                --i;
            }
        }
        return i;
    }
}
