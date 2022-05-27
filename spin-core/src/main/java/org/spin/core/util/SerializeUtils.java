package org.spin.core.util;

import org.spin.core.collection.CyclicArrayDeque;
import org.spin.core.throwable.SpinException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * java序列化/反序列化工具类
 * <p>Created by xuweinan on 2016/8/15.</p>
 *
 * @author xuweinan
 */
public final class SerializeUtils extends Util {

    private SerializeUtils() {
    }

    /**
     * 将对象序列化为字节数组
     *
     * @param object 对象
     * @return 字节数组
     */
    public static byte[] serialize(Object object) {
        CyclicArrayDeque<byte[]> bytes = new CyclicArrayDeque<>(1);
        serialize(object, ByteArrayOutputStream::new, os -> bytes.push(os.toByteArray()));
        return bytes.pop();
    }

    /**
     * 将对象序列化到指定输出流，输出后流将关闭
     *
     * @param object 对象
     * @param output 输出流提供者
     */
    public static void serialize(Object object, Supplier<OutputStream> output) {
        OutputStream os = output.get();
        try (ObjectOutputStream oos = os instanceof ObjectOutputStream ? (ObjectOutputStream) os : new ObjectOutputStream(os)) {
            oos.writeObject(object);
        } catch (IOException e) {
            throw new SpinException("对象流写入失败", e);
        }
    }

    /**
     * 将对象序列化到指定输出流，之后对该输出流执行自定义处理逻辑。完成后流将关闭
     *
     * @param object 对象
     * @param output 输出流提供者
     * @param proc   自定义处理逻辑
     * @param <T>    输出流类型参数
     */
    public static <T extends OutputStream> void serialize(Object object, Supplier<T> output, Consumer<T> proc) {
        T os = output.get();
        try (ObjectOutputStream oos = os instanceof ObjectOutputStream ? (ObjectOutputStream) os : new ObjectOutputStream(os)) {
            oos.writeObject(object);
            proc.accept(os);
        } catch (IOException e) {
            throw new SpinException("对象流写入失败", e);
        }
    }

    /**
     * 从字节数组反序列化指定类型的对象
     *
     * @param bytes 字节数组
     * @param <T>   类型参数
     * @return 反序列化的对象
     */
    public static <T> T deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return deserialize(() -> new ByteArrayInputStream(bytes));
    }

    /**
     * 从指定输入流反序列化指定类型的对象，完成后输入流将关闭
     *
     * @param input 数据流
     * @param <T>   对象类型参数
     * @return 反序列化的对象
     */
    public static <T> T deserialize(Supplier<InputStream> input) {
        if (null == input) {
            return null;
        }

        InputStream is = input.get();
        if (null == is) {
            return null;
        }

        try (ObjectInputStream ois = is instanceof ObjectInputStream ? (ObjectInputStream) is : new ObjectInputStream(is)) {
            Object o = ois.readObject();
            //noinspection unchecked
            return (T) o;
        } catch (IOException e) {
            throw new SpinException("对象流读取失败", e);
        } catch (ClassNotFoundException e) {
            throw new SpinException("序列化对象的类型不存在", e);
        } catch (ClassCastException e) {
            throw new SpinException("对象类型不匹配", e);
        }
    }
}
