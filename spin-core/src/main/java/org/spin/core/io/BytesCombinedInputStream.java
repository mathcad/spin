package org.spin.core.io;

import org.spin.core.Assert;

import java.io.IOException;
import java.io.InputStream;

/**
 * 将一个字节数组与输入流组合在一起作为一个复合流，其中字节数组的部分可以重复读取
 * <p>
 * <strong>需要注意组合一个过大的字节数组所带来的内存压力</strong>
 * </p>
 * <p>Created by xuweinan on 2018/5/5.</p>
 *
 * @author xuweinan
 */
public class BytesCombinedInputStream extends InputStream implements AutoCloseable {

    private final byte[] bytesBuf;

    private int pos = 0;

    private int count;

    private InputStream source;

    public BytesCombinedInputStream(byte[] bytes, InputStream source) {
        this.bytesBuf = Assert.notNull(bytes);
        this.count = bytes.length;
        this.source = source;
    }

    /**
     * 从输入流中读取bufLen长度的字节数，作为字节数组与流的剩余部分组合为新的流，用来实现前bufLen长度字节的重复读取
     *
     * @param source 原始流
     * @param bufLen 预读取的字节数
     * @throws IOException 流读取异常时抛出
     */
    public BytesCombinedInputStream(InputStream source, int bufLen) throws IOException {
        this.bytesBuf = new byte[bufLen];
        int total = Assert.notNull(source, "输入流不能为空").read(bytesBuf, 0, bufLen);
        if (total != bufLen) {
            throw new IOException("source read EOF");
        }
        this.count = bufLen;
        this.source = source;
    }

    @Override
    public int read() throws IOException {
        if (pos < count) {
            return bytesBuf[pos++] & 0xff;
        } else {
            pos++;
            return source.read();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (null == b) {
            return -1;
        }
        if (pos < count) {
            int diff = count - pos;
            System.arraycopy(bytesBuf, pos, b, off, diff);
            pos = count;
            int cnt = source.read(b, off + diff, b.length - diff);
            pos += cnt;
            return diff + cnt;
        } else {
            int cnt = source.read(b, off, len);
            pos += cnt;
            return cnt;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        if (pos < count) {
            int diff = count - pos;
            long skip = diff + source.skip(n - diff);
            pos += skip;
            return skip;
        } else {
            long skip = source.skip(n);
            pos += skip;
            return skip;
        }
    }

    @Override
    public int available() throws IOException {
        return (pos < count ? count - pos : 0) + source.available();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    /**
     * 读取当前复合流的字节数组部分到指定地址
     *
     * @param b 目标字节数组
     * @return 实际读取的长度，为目标数组与流中的复合数组长度的最小值
     */
    public int readCombinedBytes(byte[] b) {
        int len = b.length < bytesBuf.length ? b.length : bytesBuf.length;
        System.arraycopy(bytesBuf, 0, b, 0, len);
        return len;
    }
}
