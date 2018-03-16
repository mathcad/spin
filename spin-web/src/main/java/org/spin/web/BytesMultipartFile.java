package org.spin.web;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
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
    private final byte[] fileContent;
    private final long size;
    private final String fileName;
    private final String contentType;

    public BytesMultipartFile(byte[] fileContent) {
        this.fileContent = fileContent;
        this.size = fileContent.length;
        this.fileName = "";
        this.contentType = "";
    }

    public BytesMultipartFile(MultipartFile file) {
        try {
            this.fileContent = file.getBytes();
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.IO_FAIL, "文件读取错误");
        }
        this.size = fileContent.length;
        this.fileName = file.getName();
        this.contentType = file.getContentType();
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        String filename = this.getName();
        if (filename == null) {
            // Should never happen.
            return "";
        }
        // Check for Unix-style path
        int unixSep = filename.lastIndexOf('/');
        // Check for Windows-style path
        int winSep = filename.lastIndexOf('\\');
        // Cut off at latest possible point
        int pos = (winSep > unixSep ? winSep : unixSep);
        if (pos != -1) {
            // Any sort of path separator found...
            return filename.substring(pos + 1);
        } else {
            // A plain name
            return filename;
        }
    }

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

    @Override
    public byte[] getBytes() {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (OutputStream os = new FileOutputStream(dest)) {
            os.write(fileContent);
        }
    }
}
