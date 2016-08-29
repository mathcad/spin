package org.zibra.server;

import org.zibra.util.concurrent.Promise;

class Message {
    public final Promise<Boolean> detector;
    public final Object result;

    public Message(Promise<Boolean> detector, Object result) {
        this.detector = detector;
        this.result = result;
    }
}
