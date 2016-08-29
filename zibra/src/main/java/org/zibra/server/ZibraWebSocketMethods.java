package org.zibra.server;

import org.zibra.common.HproseMethods;
import org.zibra.common.ZibraContext;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.lang.reflect.Type;

public class ZibraWebSocketMethods extends HproseMethods {

    @Override
    protected int getCount(Type[] paramTypes) {
        int i = paramTypes.length;
        if ((i > 0) && (paramTypes[i - 1] instanceof Class<?>)) {
            Class<?> paramType = (Class<?>) paramTypes[i - 1];
            if (paramType.equals(ZibraContext.class) ||
                    paramType.equals(ServiceContext.class) ||
                    paramType.equals(WebSocketContext.class) ||
                    paramType.equals(EndpointConfig.class) ||
                    paramType.equals(Session.class)) {
                --i;
            }
        }
        return i;
    }
}
