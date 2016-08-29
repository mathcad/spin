package org.zibra.server;

import org.zibra.common.ZibraContext;

public interface ZibraServiceEvent {
    void onBeforeInvoke(String name, Object[] args, boolean byRef, ZibraContext context) throws Throwable;

    void onAfterInvoke(String name, Object[] args, boolean byRef, Object result, ZibraContext context) throws Throwable;

    Throwable onSendError(Throwable e, ZibraContext context) throws Throwable;

    void onServerError(Throwable e, ZibraContext context);
}
