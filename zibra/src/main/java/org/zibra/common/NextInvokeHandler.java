package org.zibra.common;

import org.zibra.util.concurrent.Promise;

public interface NextInvokeHandler {
    Promise<Object> handle(String name, Object[] args, ZibraContext context);
}
