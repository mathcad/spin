package org.spin.core.util.excel;

import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.io.BytesCombinedInputStream;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.file.FileType;
import org.spin.core.util.file.FileTypeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Excel工具类(基于SaxReader与事件驱动，支持大文件)
 *
 * @author wangy QQ 837195190
 * <p>Created by thinkpad on 2018/5/4.</p>
 */
public abstract class ExcelUtils {
    private static final int TRAIT_LEN = 16;

    private ExcelUtils() {
    }

    public static void readWorkBook(InputStream is, FinalConsumer<ExcelRow> rowReader) {
        byte[] trait = new byte[TRAIT_LEN];
        int read;
        FileType fileType;
        BytesCombinedInputStream bcis;
        try {
            bcis = new BytesCombinedInputStream(is, TRAIT_LEN);
            read = bcis.readCombinedBytes(trait);
            if (read < TRAIT_LEN) {
                throw new SimplifiedException(ErrorCode.IO_FAIL, "输入流中不包含有效内容");
            }
            fileType = FileTypeUtils.detectFileType(trait);
            ExcelReader reader;
            if (Objects.isNull(fileType)) {
                throw new SimplifiedException(ErrorCode.IO_FAIL, "不支持的文件类型");
            } else if (fileType.equals(FileType.Document.XLS)) {
                reader = new ExcelXlsReader();
            } else if (fileType.equals(FileType.Document.XLSX)) {
                reader = new ExcelXlsxReader();
            } else {
                throw new SimplifiedException(ErrorCode.IO_FAIL, "不支持的文件类型");
            }
            reader.process(bcis, rowReader);
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "输入流读取失败", e);
        }
    }

    public static void readWorkBook(String fileName, FinalConsumer<ExcelRow> rowReader) {
        readWorkBook(new File(fileName), rowReader);
    }

    public static void readWorkBook(File file, FinalConsumer<ExcelRow> rowReader) {
        try (InputStream is = new FileInputStream(file)) {
            readWorkBook(is, rowReader);
        } catch (FileNotFoundException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "读取的文件不存在", e);
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "读取文件失败", e);
        }
    }

}


