package org.spin.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * 基于字节数组的MultipartFile
 * <p>无临时文件，全部暂存于内存中用来解决跨服务器传输MultipartFile的问题</p>
 * Created by xuweinan on 2017/1/9.
 *
 * @author xuweinan
 */
public class BytesMultipartFile implements MultipartFile, Serializable {
    private static final long serialVersionUID = 3858250164636170128L;
    private static final int FILE_LIMIT = 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(BytesMultipartFile.class);

    private final byte[] fileContent;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public BytesMultipartFile(byte[] fileContent, String name) {
        this(fileContent, name, null, "");
    }

    public BytesMultipartFile(byte[] fileContent, String name, String originalFilename) {
        this(fileContent, name, originalFilename, "");
    }

    public BytesMultipartFile(byte[] fileContent, String name, String originalFilename, String contentType) {
        Assert.notNull(fileContent, "文件内容不能为空");
        if (fileContent.length > FILE_LIMIT) {
            logger.warn("BytesMultipartFile文件过大，存在内存泄露风险 [name:{}, size: {}]", name, fileContent.length);
        }
        Assert.notEmpty(name, "文件参数名称不能为空");
        this.fileContent = fileContent;
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    public BytesMultipartFile(MultipartFile file) {
        try {
            this.fileContent = file.getBytes();
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "文件读取错误");
        }
        this.name = file.getName();
        this.originalFilename = file.getOriginalFilename();
        this.contentType = file.getContentType();
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Nullable
    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent != null && fileContent.length > 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @NonNull
    @Override
    public byte[] getBytes() {
        return fileContent;
    }

    @NonNull
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException {
        try (OutputStream os = new FileOutputStream(dest)) {
            os.write(fileContent);
        }
    }
}
