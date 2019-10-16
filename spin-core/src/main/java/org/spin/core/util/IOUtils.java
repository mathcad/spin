package org.spin.core.util;

import org.spin.core.Assert;
import org.spin.core.io.StreamProgress;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Java输入输出流的工具类. 所有方法的block size都是4096字节.
 *
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Brian Clozel
 * @author xuweinan
 */
public abstract class IOUtils {

    public static final int BUFFER_SIZE = 4096;

    private static final byte[] EMPTY_CONTENT = new byte[0];

    /**
     * 数据流末尾
     */
    public static final int EOF = -1;

    /**
     * 将Reader中的内容复制到Writer中 使用默认缓存大小
     *
     * @param reader Reader
     * @param writer Writer
     * @return 拷贝的字节数
     * @throws IOException IO异常
     */
    public static long copy(Reader reader, Writer writer) throws IOException {
        return copy(reader, writer, BUFFER_SIZE);
    }

    /**
     * 将Reader中的内容复制到Writer中
     *
     * @param reader     Reader
     * @param writer     Writer
     * @param bufferSize 缓存大小
     * @return 传输的byte数
     * @throws IOException IO异常
     */
    public static long copy(Reader reader, Writer writer, int bufferSize) throws IOException {
        return copy(reader, writer, bufferSize, null);
    }

    /**
     * 将Reader中的内容复制到Writer中
     *
     * @param reader         Reader
     * @param writer         Writer
     * @param bufferSize     缓存大小
     * @param streamProgress 进度处理器
     * @return 传输的byte数
     * @throws IOException IO异常
     */
    public static long copy(Reader reader, Writer writer, int bufferSize, StreamProgress streamProgress) throws IOException {
        char[] buffer = new char[bufferSize];
        long size = 0;
        int readSize;
        if (null != streamProgress) {
            streamProgress.start();
        }
        try {
            while ((readSize = reader.read(buffer, 0, bufferSize)) != EOF) {
                writer.write(buffer, 0, readSize);
                size += readSize;
                writer.flush();
                if (null != streamProgress) {
                    streamProgress.progress(size);
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        if (null != streamProgress) {
            streamProgress.finish();
        }
        return size;
    }

    /**
     * 拷贝流
     *
     * @param in         输入流
     * @param out        输出流
     * @param bufferSize 缓存大小
     * @return 传输的byte数
     * @throws IOException IO异常
     */
    public static long copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        return copy(in, out, bufferSize, null);
    }

    /**
     * 拷贝流
     *
     * @param in             输入流
     * @param out            输出流
     * @param bufferSize     缓存大小
     * @param streamProgress 进度条
     * @return 传输的byte数
     * @throws IOException IO异常
     */
    public static long copy(InputStream in, OutputStream out, int bufferSize, StreamProgress streamProgress) throws IOException {
        Assert.notNull(in, "InputStream is null !");
        Assert.notNull(out, "OutputStream is null !");
        if (bufferSize <= 0) {
            bufferSize = BUFFER_SIZE;
        }

        byte[] buffer = new byte[bufferSize];
        if (null != streamProgress) {
            streamProgress.start();
        }
        long size = 0;
        for (int readSize; (readSize = in.read(buffer)) != EOF; ) {
            out.write(buffer, 0, readSize);
            size += readSize;
            out.flush();
            if (null != streamProgress) {
                streamProgress.progress(size);
            }
        }
        if (null != streamProgress) {
            streamProgress.finish();
        }
        return size;
    }

    /**
     * 拷贝流
     * 本方法不会关闭流
     *
     * @param in             输入流
     * @param out            输出流
     * @param bufferSize     缓存大小
     * @param streamProgress 进度条
     * @return 传输的byte数
     * @throws IOException IO异常
     */
    public static long copyByNIO(InputStream in, OutputStream out, int bufferSize, StreamProgress streamProgress) throws IOException {
        return copy(Channels.newChannel(in), Channels.newChannel(out), bufferSize, streamProgress);
    }

    /**
     * 拷贝文件流，使用NIO
     *
     * @param in  输入
     * @param out 输出
     * @return 拷贝的字节数
     * @throws IOException IO异常
     */
    public static long copy(FileInputStream in, FileOutputStream out) throws IOException {
        Assert.notNull(in, "FileInputStream is null!");
        Assert.notNull(out, "FileOutputStream is null!");

        final FileChannel inChannel = in.getChannel();
        final FileChannel outChannel = out.getChannel();

        return inChannel.transferTo(0, inChannel.size(), outChannel);
    }

    /**
     * 拷贝流，使用NIO，不会关闭流
     *
     * @param in  {@link ReadableByteChannel}
     * @param out {@link WritableByteChannel}
     * @return 拷贝的字节数
     * @throws IOException IO异常
     * @since 4.5.0
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out) throws IOException {
        return copy(in, out, BUFFER_SIZE);
    }

    /**
     * 拷贝流，使用NIO，不会关闭流
     *
     * @param in         {@link ReadableByteChannel}
     * @param out        {@link WritableByteChannel}
     * @param bufferSize 缓冲大小，如果小于等于0，使用默认
     * @return 拷贝的字节数
     * @throws IOException IO异常
     * @since 4.5.0
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize) throws IOException {
        return copy(in, out, bufferSize, null);
    }

    /**
     * 拷贝流，使用NIO，不会关闭流
     *
     * @param in             {@link ReadableByteChannel}
     * @param out            {@link WritableByteChannel}
     * @param bufferSize     缓冲大小，如果小于等于0，使用默认
     * @param streamProgress {@link StreamProgress}进度处理器
     * @return 拷贝的字节数
     * @throws IOException IO异常
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize, StreamProgress streamProgress) throws IOException {
        Assert.notNull(in, "InputStream is null !");
        Assert.notNull(out, "OutputStream is null !");

        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize <= 0 ? BUFFER_SIZE : bufferSize);
        long size = 0;
        if (null != streamProgress) {
            streamProgress.start();
        }
        while (in.read(byteBuffer) != EOF) {
            byteBuffer.flip();// 写转读
            size += out.write(byteBuffer);
            byteBuffer.clear();
            if (null != streamProgress) {
                streamProgress.progress(size);
            }
        }
        if (null != streamProgress) {
            streamProgress.finish();
        }

        return size;
    }

    /**
     * 将输入流中的内容读取到字节数组中. 当输入流内容很大或者无法预料时请慎用，以免过渡消耗内存
     *
     * @param in 需要读取的输入流(可以为 {@code null})
     * @return 包含了输入流所有内容的字节数组
     * @throws IOException IO异常时抛出
     */
    public static byte[] copyToByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }

    /**
     * 读取输入流中的所有内容, 转换为指定字符集下的字符串
     *
     * @param in      需要读取的输入流(可以为 {@code null})
     * @param charset 字符集
     * @return 包含输入流所有内容的字符串
     * @throws IOException IO异常时抛出
     */
    public static String copyToString(InputStream in, Charset charset) throws IOException {
        if (in == null) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }

    /**
     * Copy the contents of the given byte array to the given OutputStream.
     * Leaves the stream open when done.
     *
     * @param in  the byte array to copy from
     * @param out the OutputStream to copy to
     * @throws IOException in case of I/O errors
     */
    public static void copy(byte[] in, OutputStream out) throws IOException {
        Assert.notNull(in, "No input byte array specified");
        Assert.notNull(out, "No OutputStream specified");

        out.write(in);
    }

    /**
     * Copy the contents of the given String to the given output OutputStream.
     * Leaves the stream open when done.
     *
     * @param in      the String to copy from
     * @param charset the Charset
     * @param out     the OutputStream to copy to
     * @throws IOException in case of I/O errors
     */
    public static void copy(String in, Charset charset, OutputStream out) throws IOException {
        Assert.notNull(in, "No input String specified");
        Assert.notNull(charset, "No charset specified");
        Assert.notNull(out, "No OutputStream specified");

        Writer writer = new OutputStreamWriter(out, charset);
        writer.write(in);
        writer.flush();
    }

    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Leaves both streams open when done.
     *
     * @param in  the InputStream to copy from
     * @param out the OutputStream to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");

        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }

    /**
     * Copy a range of content of the given InputStream to the given OutputStream.
     * <p>If the specified range exceeds the length of the InputStream, this copies
     * up to the end of the stream and returns the actual number of copied bytes.
     * <p>Leaves both streams open when done.
     *
     * @param in    the InputStream to copy from
     * @param out   the OutputStream to copy to
     * @param start the position to start copying from
     * @param end   the position to end copying
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     * @since 4.3
     */
    public static long copyRange(InputStream in, OutputStream out, long start, long end) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");

        long skipped = in.skip(start);
        if (skipped < start) {
            throw new IOException("Skipped only " + skipped + " bytes out of " + start + " required");
        }

        long bytesToCopy = end - start + 1;
        byte[] buffer = new byte[IOUtils.BUFFER_SIZE];
        while (bytesToCopy > 0) {
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) {
                break;
            } else if (bytesRead <= bytesToCopy) {
                out.write(buffer, 0, bytesRead);
                bytesToCopy -= bytesRead;
            } else {
                out.write(buffer, 0, (int) bytesToCopy);
                bytesToCopy = 0;
            }
        }
        return (end - start + 1 - bytesToCopy);
    }

    /**
     * Drain the remaining content of the given InputStream.
     * Leaves the InputStream open when done.
     *
     * @param in the InputStream to drain
     * @return the number of bytes read
     * @throws IOException in case of I/O errors
     * @since 4.3
     */
    public static int drain(InputStream in) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        int byteCount = 0;
        while ((bytesRead = in.read(buffer)) != -1) {
            byteCount += bytesRead;
        }
        return byteCount;
    }

    /**
     * Return an efficient empty {@link InputStream}.
     *
     * @return a {@link ByteArrayInputStream} based on an empty byte array
     * @since 4.2.2
     */
    public static InputStream emptyInput() {
        return new ByteArrayInputStream(EMPTY_CONTENT);
    }

    /**
     * Return a variant of the given {@link InputStream} where calling
     * {@link InputStream#close() close()} has no effect.
     *
     * @param in the InputStream to decorate
     * @return a version of the InputStream that ignores calls to close
     */
    public static InputStream nonClosing(InputStream in) {
        Assert.notNull(in, "No InputStream specified");
        return new NonClosingInputStream(in);
    }

    /**
     * Return a variant of the given {@link OutputStream} where calling
     * {@link OutputStream#close() close()} has no effect.
     *
     * @param out the OutputStream to decorate
     * @return a version of the OutputStream that ignores calls to close
     */
    public static OutputStream nonClosing(OutputStream out) {
        Assert.notNull(out, "No OutputStream specified");
        return new NonClosingOutputStream(out);
    }

    /**
     * 无条件关闭一个 <code>Closeable</code>.
     * <p>
     * 与 {@link Closeable#close()}效果相同, 但会忽略任何异常.
     * 通常在finally代码块中使用该方法.
     * <p>
     * 示例:
     * <pre>
     *   Closeable closeable = null;
     *   try {
     *       closeable = new FileReader("foo.txt");
     *       // process closeable
     *       closeable.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(closeable);
     *   }
     * </pre>
     *
     * @param closeable the object to close, may be null or already closed
     * @since 2.0
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * 无条件关闭一个 <code>AutoCloseable</code>.
     * <p>
     * 与 {@link AutoCloseable#close()}效果相同, 但会忽略任何异常.
     * 通常在finally代码块中使用该方法.
     * <p>
     * 示例:
     * <pre>
     *   AutoCloseable closeable = null;
     *   try {
     *       closeable = new FileReader("foo.txt");
     *       // process closeable
     *       closeable.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(closeable);
     *   }
     * </pre>
     *
     * @param closeable the object to close, may be null or already closed
     * @since 2.0
     */
    public static void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ioe) {
            // ignore
        }
    }

    /**
     * Returns the given reader if it is a {@link BufferedReader}, otherwise creates a toBufferedReader for the given
     * reader.
     *
     * @param reader the reader to wrap or return
     * @return the given reader or a new {@link BufferedReader} for the given reader
     * @since 2.2
     */
    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Get the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line, using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static List<String> readLines(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);
        return readLines(reader);
    }

    /**
     * Get the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input    the <code>InputStream</code> to read from, not null
     * @param encoding the encoding to use, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static List<String> readLines(InputStream input, String encoding) throws IOException {
        if (encoding == null) {
            return readLines(input);
        } else {
            InputStreamReader reader = new InputStreamReader(input, encoding);
            return readLines(reader);
        }
    }

    /**
     * Get the contents of a <code>Reader</code> as a list of Strings,
     * one entry per line.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader</code>.
     *
     * @param input the <code>Reader</code> to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    /**
     * Convert the specified CharSequence to an input stream, encoded as bytes
     * using the default character encoding of the platform.
     *
     * @param input the CharSequence to convert
     * @return an input stream
     * @since 2.0
     */
    public static InputStream toInputStream(CharSequence input) {
        return toInputStream(input.toString());
    }

    /**
     * Convert the specified CharSequence to an input stream, encoded as bytes
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     *
     * @param input    the CharSequence to convert
     * @param encoding the encoding to use, null means platform default
     * @return an input stream
     * @throws IOException if the encoding is invalid
     * @since 2.0
     */
    public static InputStream toInputStream(CharSequence input, String encoding) throws IOException {
        return toInputStream(input.toString(), encoding);
    }

    //-----------------------------------------------------------------------

    /**
     * Convert the specified string to an input stream, encoded as bytes
     * using the default character encoding of the platform.
     *
     * @param input the string to convert
     * @return an input stream
     * @since 1.1
     */
    public static InputStream toInputStream(String input) {
        byte[] bytes = input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Convert the specified string to an input stream, encoded as bytes
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     *
     * @param input    the string to convert
     * @param encoding the encoding to use, null means platform default
     * @return an input stream
     * @throws IOException if the encoding is invalid
     * @since 1.1
     */
    public static InputStream toInputStream(String input, String encoding) throws IOException {
        byte[] bytes = encoding != null ? input.getBytes(encoding) : input.getBytes();
        return new ByteArrayInputStream(bytes);
    }


    private static class NonClosingInputStream extends FilterInputStream {

        public NonClosingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() {
        }
    }


    private static class NonClosingOutputStream extends FilterOutputStream {

        public NonClosingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b, int off, int let) throws IOException {
            // It is critical that we override this method for performance
            out.write(b, off, let);
        }

        @Override
        public void close() {
        }
    }

}
