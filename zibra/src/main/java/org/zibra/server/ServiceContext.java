package org.zibra.server;

import org.zibra.common.ZibraContext;
import org.zibra.common.HproseMethod;
import org.zibra.common.HproseMethods;

public class ServiceContext extends ZibraContext {
    private HproseMethod remoteMethod = null;
    private HproseMethods methods = null;
    private boolean missingMethod = false;
    private boolean byref = false;
    public final ZibraClients clients;

    protected ServiceContext(ZibraClients clients) {
        this.clients = clients;
    }

    public HproseMethod getRemoteMethod() {
        return remoteMethod;
    }

    public void setRemoteMethod(HproseMethod method) {
        remoteMethod = method;
    }

    public HproseMethods getMethods() {
        return methods;
    }

    public void setMethods(HproseMethods methods) {
        this.methods = methods;
    }

    public boolean isMissingMethod() {
        return missingMethod;
    }

    public void setMissingMethod(boolean missingMethod) {
        this.missingMethod = missingMethod;
    }

    public boolean isByref() {
        return byref;
    }

    public void setByref(boolean byref) {
        this.byref = byref;
    }

}