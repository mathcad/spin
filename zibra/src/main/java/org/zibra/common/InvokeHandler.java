package org.zibra.common;


import org.zibra.util.concurrent.Promise;

public interface InvokeHandler {
    Promise<Object> handle(String name, Object[] args, ZibraContext context, NextInvokeHandler next);
}
