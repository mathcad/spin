package org.zibra.client;


import org.zibra.common.ZibraContext;
import org.zibra.common.InvokeSettings;

public class ClientContext extends ZibraContext {
    private final ZibraClient client;
    private final InvokeSettings settings;

    public ClientContext(ZibraClient client) {
        this.client = client;
        settings = new InvokeSettings();
        settings.setByref(client.isByref());
        settings.setSimple(client.isSimple());
        settings.setFailswitch(client.isFailswitch());
        settings.setIdempotent(client.isIdempotent());
        settings.setRetry(client.getRetry());
        settings.setTimeout(client.getTimeout());
        settings.setOneway(false);
    }

    public ZibraClient getClient() {
        return client;
    }

    public InvokeSettings getSettings() {
        return settings;
    }
}