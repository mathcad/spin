package org.zibra.io;

import org.zibra.io.serialize.ValueWriter;
import org.zibra.io.serialize.Writer;
import org.zibra.io.unserialize.Reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Formatter {

    static class Cache {
        Object obj;
        ZibraMode mode;
        boolean simple;
        byte[] buffer;

        public Cache(Object o, ZibraMode m, boolean s, byte[] b) {
            obj = o;
            mode = m;
            simple = s;
            buffer = b;
        }
    }

    private static ThreadLocal<Cache> cache = new ThreadLocal<>();

    private Formatter() {
    }

    public static OutputStream serialize(byte b, OutputStream stream) throws IOException {
        ValueWriter.write(stream, b);
        return stream;
    }

    public static OutputStream serialize(short s, OutputStream stream) throws IOException {
        ValueWriter.write(stream, s);
        return stream;
    }

    public static OutputStream serialize(int i, OutputStream stream) throws IOException {
        ValueWriter.write(stream, i);
        return stream;
    }

    public static OutputStream serialize(long l, OutputStream stream) throws IOException {
        ValueWriter.write(stream, l);
        return stream;

    }

    public static OutputStream serialize(float f, OutputStream stream) throws IOException {
        ValueWriter.write(stream, f);
        return stream;

    }

    public static OutputStream serialize(double d, OutputStream stream) throws IOException {
        ValueWriter.write(stream, d);
        return stream;

    }

    public static OutputStream serialize(boolean b, OutputStream stream) throws IOException {
        ValueWriter.write(stream, b);
        return stream;

    }

    public static OutputStream serialize(char c, OutputStream stream) throws IOException {
        ValueWriter.write(stream, c);
        return stream;
    }

    public static OutputStream serialize(BigInteger bi, OutputStream stream) throws IOException {
        ValueWriter.write(stream, bi);
        return stream;
    }

    public static OutputStream serialize(BigDecimal bd, OutputStream stream) throws IOException {
        ValueWriter.write(stream, bd);
        return stream;
    }

    public static OutputStream serialize(Object obj, OutputStream stream) throws IOException {
        return serialize(obj, stream, ZibraMode.MemberMode, false);
    }

    public static OutputStream serialize(Object obj, OutputStream stream, boolean simple) throws IOException {
        return serialize(obj, stream, ZibraMode.MemberMode, simple);
    }

    public static OutputStream serialize(Object obj, OutputStream stream, ZibraMode mode) throws IOException {
        return serialize(obj, stream, mode, false);
    }

    public static OutputStream serialize(Object obj, OutputStream stream, ZibraMode mode, boolean simple) throws IOException {
        Writer writer = new Writer(stream, mode, simple);
        writer.serialize(obj);
        return stream;
    }

    public static ByteBufferStream serialize(byte b) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream(8);
        serialize(b, bufstream.getOutputStream());
        bufstream.flip();
        return bufstream;
    }

    public static ByteBufferStream serialize(short s) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream(8);
        serialize(s, bufstream.getOutputStream());
        bufstream.flip();
        return bufstream;
    }

    public static ByteBufferStream serialize(int i) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream(16);
        serialize(i, bufstream.getOutputStream());
        bufstream.flip();
        return bufstream;
    }

    public static ByteBufferStream serialize(long l) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream(32);
        serialize(l, bufstream.getOutputStream());
        bufstream.flip();
        return bufstream;
    }

    public static ByteBufferStream serialize(float f) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream(32);
        serialize(f, bufstream.getOutputStream());
        bufstream.flip();
        return bufstream;
    }

    public static ByteBufferStream serialize(double d) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream(32);
        serialize(d, bufstream.getOutputStream());
        bufstream.flip();
        return bufstream;
    }

    public static ByteBufferStream serialize(boolean b) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream(1);
        serialize(b, bufstream.getOutputStream());
        bufstream.flip();
        return bufstream;
    }

    public static ByteBufferStream serialize(char c) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream(4);
        serialize(c, bufstream.getOutputStream());
        bufstream.flip();
        return bufstream;
    }

    public static ByteBufferStream serialize(Object obj) throws IOException {
        return serialize(obj, ZibraMode.MemberMode, false);
    }

    public static ByteBufferStream serialize(Object obj, ZibraMode mode) throws IOException {
        return serialize(obj, mode, false);
    }

    public static ByteBufferStream serialize(Object obj, boolean simple) throws IOException {
        return serialize(obj, ZibraMode.MemberMode, simple);
    }

    public static ByteBufferStream serialize(Object obj, ZibraMode mode, boolean simple) throws IOException {
        ByteBufferStream bufstream = new ByteBufferStream();
        Cache c = cache.get();
        if ((c != null) &&
                (obj == c.obj) &&
                (mode == c.mode) &&
                (simple == c.simple)) {
            bufstream.write(c.buffer);
            return bufstream;
        } else {
            serialize(obj, bufstream.getOutputStream(), mode, simple);
            cache.set(new Cache(obj, mode, simple, bufstream.toArray()));
            bufstream.flip();
            return bufstream;
        }
    }

    public static Object unserialize(ByteBufferStream stream) throws IOException {
        Reader reader = new Reader(stream.buffer);
        return reader.unserialize();
    }

    public static Object unserialize(ByteBufferStream stream, ZibraMode mode) throws IOException {
        Reader reader = new Reader(stream.buffer, mode);
        return reader.unserialize();
    }

    public static Object unserialize(ByteBufferStream stream, boolean simple) throws IOException {
        Reader reader = new Reader(stream.buffer, simple);
        return reader.unserialize();
    }

    public static Object unserialize(ByteBufferStream stream, ZibraMode mode, boolean simple) throws IOException {
        Reader reader = new Reader(stream.buffer, mode, simple);
        return reader.unserialize();
    }

    public static <T> T unserialize(ByteBufferStream stream, Class<T> type) throws IOException {
        Reader reader = new Reader(stream.buffer);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(ByteBufferStream stream, ZibraMode mode, Class<T> type) throws IOException {
        Reader reader = new Reader(stream.buffer, mode);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(ByteBufferStream stream, boolean simple, Class<T> type) throws IOException {
        Reader reader = new Reader(stream.buffer, simple);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(ByteBufferStream stream, ZibraMode mode, boolean simple, Class<T> type) throws IOException {
        Reader reader = new Reader(stream.buffer, mode, simple);
        return reader.unserialize(type);
    }

    public static Object unserialize(ByteBuffer data) throws IOException {
        Reader reader = new Reader(data);
        return reader.unserialize();
    }

    public static Object unserialize(ByteBuffer data, ZibraMode mode) throws IOException {
        Reader reader = new Reader(data, mode);
        return reader.unserialize();
    }

    public static Object unserialize(ByteBuffer data, boolean simple) throws IOException {
        Reader reader = new Reader(data, simple);
        return reader.unserialize();
    }

    public static Object unserialize(ByteBuffer data, ZibraMode mode, boolean simple) throws IOException {
        Reader reader = new Reader(data, mode, simple);
        return reader.unserialize();
    }

    public static <T> T unserialize(ByteBuffer data, Class<T> type) throws IOException {
        Reader reader = new Reader(data);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(ByteBuffer data, ZibraMode mode, Class<T> type) throws IOException {
        Reader reader = new Reader(data, mode);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(ByteBuffer data, boolean simple, Class<T> type) throws IOException {
        Reader reader = new Reader(data, simple);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(ByteBuffer data, ZibraMode mode, boolean simple, Class<T> type) throws IOException {
        Reader reader = new Reader(data, mode, simple);
        return reader.unserialize(type);
    }

    public static Object unserialize(byte[] data) throws IOException {
        Reader reader = new Reader(data);
        return reader.unserialize();
    }

    public static Object unserialize(byte[] data, ZibraMode mode) throws IOException {
        Reader reader = new Reader(data, mode);
        return reader.unserialize();
    }

    public static Object unserialize(byte[] data, boolean simple) throws IOException {
        Reader reader = new Reader(data, simple);
        return reader.unserialize();
    }

    public static Object unserialize(byte[] data, ZibraMode mode, boolean simple) throws IOException {
        Reader reader = new Reader(data, mode, simple);
        return reader.unserialize();
    }

    public static <T> T unserialize(byte[] data, Class<T> type) throws IOException {
        Reader reader = new Reader(data);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(byte[] data, ZibraMode mode, Class<T> type) throws IOException {
        Reader reader = new Reader(data, mode);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(byte[] data, boolean simple, Class<T> type) throws IOException {
        Reader reader = new Reader(data, simple);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(byte[] data, ZibraMode mode, boolean simple, Class<T> type) throws IOException {
        Reader reader = new Reader(data, mode, simple);
        return reader.unserialize(type);
    }

    public static Object unserialize(InputStream stream) throws IOException {
        Reader reader = new Reader(stream);
        return reader.unserialize();
    }

    public static Object unserialize(InputStream stream, ZibraMode mode) throws IOException {
        Reader reader = new Reader(stream, mode);
        return reader.unserialize();
    }

    public static Object unserialize(InputStream stream, boolean simple) throws IOException {
        Reader reader = new Reader(stream, simple);
        return reader.unserialize();
    }

    public static Object unserialize(InputStream stream, ZibraMode mode, boolean simple) throws IOException {
        Reader reader = new Reader(stream, mode, simple);
        return reader.unserialize();
    }

    public static <T> T unserialize(InputStream stream, Class<T> type) throws IOException {
        Reader reader = new Reader(stream);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(InputStream stream, ZibraMode mode, Class<T> type) throws IOException {
        Reader reader = new Reader(stream, mode);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(InputStream stream, boolean simple, Class<T> type) throws IOException {
        Reader reader = new Reader(stream, simple);
        return reader.unserialize(type);
    }

    public static <T> T unserialize(InputStream stream, ZibraMode mode, boolean simple, Class<T> type) throws IOException {
        Reader reader = new Reader(stream, mode, simple);
        return reader.unserialize(type);
    }
}
