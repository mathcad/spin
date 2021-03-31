package org.spin.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * 基于快速缓冲FastByteBuffer的OutputStream，随着数据的增长自动扩充缓冲区
 * <p>
 * 可以通过{@link #toByteArray()}和 {@link #toString()}来获取数据
 * <p>
 * {@link #close()}方法无任何效果，当流被关闭后不会抛出IOException
 * <p>
 * 这种设计避免重新分配内存块而是分配新增的缓冲区，缓冲区不会被GC，数据也不会被拷贝到其他缓冲区。
 */
public class FastByteBufferInputStream extends InputStream {

    private final FastByteBuffer buffer;
    private int pos;

    public FastByteBufferInputStream() {
        this(1024);
    }

    public FastByteBufferInputStream(int size) {
        buffer = new FastByteBuffer(size);
    }

    public FastByteBufferInputStream(FastByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() {
        return (pos < buffer.size()) ? buffer.get(pos++) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (pos < buffer.size()) {
            int l = buffer.copyToArray(pos, len, b, off);
            pos += l;
            return l;
        } else {
            return -1;
        }
    }

    @Override
    public int available() {
        return buffer.size() - pos;
    }

    @Override
    public long skip(long n) {
        long p = pos + n;
        p = p > buffer.size() ? buffer.size() : p;
        long c = p - pos;
        pos = (int) p;
        return c;
    }

    public int size() {
        return buffer.size();
    }

    /**
     * 此方法无任何效果，当流被关闭后不会抛出IOException
     */
    @Override
    public void close() {
        // nop
    }

    @Override
    public void reset() {
        pos = 0;
    }

    public void writeTo(OutputStream out) throws IOException {
        int index = buffer.index();
        for (int i = 0; i < index; i++) {
            byte[] buf = buffer.array(i);
            out.write(buf);
        }
        out.write(buffer.array(index), 0, buffer.offset());
    }

    public byte[] toByteArray() {
        return buffer.toArray();
    }

    @Override
    public String toString() {
        return new String(toByteArray());
    }

    public String toString(String enc) throws UnsupportedEncodingException {
        return new String(toByteArray(), enc);
    }

    public String toString(Charset charset) {
        return new String(toByteArray(), charset);
    }
}
