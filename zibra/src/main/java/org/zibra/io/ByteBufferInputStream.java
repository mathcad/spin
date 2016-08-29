package org.zibra.io;

import java.io.IOException;
import java.io.InputStream;

public final class ByteBufferInputStream extends InputStream {
    public final ByteBufferStream stream;
    ByteBufferInputStream(ByteBufferStream stream) {
        this.stream = stream;
    }

    @Override
    public final int read() throws IOException {
        return stream.read();
    }

    @Override
    public final int read(byte b[]) throws IOException {
        return stream.read(b);
    }

    @Override
    public final int read(byte b[], int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    @Override
    public final long skip(long n) throws IOException {
        return stream.skip(n);
    }

    @Override
    public final int available() throws IOException {
	return stream.available();
    }

    @Override
    public final boolean markSupported() {
	return stream.markSupported();
    }

    @Override
    public final synchronized void mark(int readlimit) {
	stream.mark(readlimit);
    }

    @Override
    public final synchronized void reset() throws IOException {
        stream.reset();
    }

    @Override
    public final void close() throws IOException {
        stream.close();
    }
}
