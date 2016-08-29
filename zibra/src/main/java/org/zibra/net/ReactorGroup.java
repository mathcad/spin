package org.zibra.net;

import java.io.IOException;

public class ReactorGroup {
    private final Reactor[] reactors;
    private int index;

    public ReactorGroup(int count) throws IOException {
        reactors = new Reactor[count];
        for (int i = 0; i < count; ++i) {
            reactors[i] = new Reactor();
        }
    }

    public void start() {
        int n = reactors.length;
        for (int i = 0; i < n; ++i) {
            reactors[i].start();
        }
    }

    public void register(Connection conn) {
        int n = reactors.length;
        index = (index + 1) % n;
        reactors[index].register(conn);
    }

    public void close() {
        for (int i = reactors.length - 1; i >= 0; --i) {
            reactors[i].close();
        }
    }
}
