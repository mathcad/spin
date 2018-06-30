package org.spin.core.util.excel;

import org.spin.core.function.FinalConsumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Excel读取器
 * <p>读取Excel内容并以行为单位执行自定义逻辑</p>
 * <p>Created by xuweinan on 2018/6/30.</p>
 *
 * @author xuweinan
 */
public interface ExcelReader {

    /**
     * 读取输入流中的内容，解析为以行为单位的数据集合({@link ExcelRow})，并对每一行调用用户自定义处理
     *
     * @param is        包含Excel文件内容的输入流
     * @param rowReader 行数据处理器
     * @throws IOException 发生流读取或文件异常时抛出
     */
    void process(InputStream is, FinalConsumer<ExcelRow> rowReader) throws IOException;

    /**
     * 读取文件中的内容，解析为以行为单位的数据集合({@link ExcelRow})，并对每一行调用用户自定义处理
     *
     * @param file      Excel文件
     * @param rowReader 行数据处理器
     * @throws IOException 文件读取异常时抛出
     */
    default void process(File file, FinalConsumer<ExcelRow> rowReader) throws IOException {
        process(new FileInputStream(file), rowReader);
    }
}
