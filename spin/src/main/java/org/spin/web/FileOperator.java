package org.spin.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.sys.EnvCache;
import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;
import org.spin.util.HttpUtils;
import org.spin.util.RandomStringUtils;
import org.spin.util.StringUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 服务器端文件操作工具类
 * <p>Created by xuweinan on 2016/10/4.</p>
 *
 * @author xuweinan
 */
public class FileOperator {
    private static final Logger logger = LoggerFactory.getLogger(FileOperator.class);
    private static final String FULLNAME = "fullName";
    private static final String STORENAME = "storeName";
    private static final String SIZE = "size";
    private static final String SUFFIX = "suffix";
    private static final String CONTENTTYPE = "contentType";
    private static final String CREATETIME = "createTime";
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMM/ddHHmmss");

    /**
     * 上传文件,默认上传到Env.FileUploadDir目录
     */
    public static UploadResult upload(MultipartFile file, boolean compress, String... baseDir) throws IOException {
        String bDir = EnvCache.FileUploadDir;
        if (null != baseDir && baseDir.length > 0 && StringUtils.isNotBlank(baseDir[0]))
            bDir = baseDir[0];
        String fileName = file.getOriginalFilename();
        String extention = compress ? ".zip" : (fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')
        ).toLowerCase() : "");
        String storeName = generateFileName();
        String path = storeName.substring(0, storeName.indexOf("/") + 1);
        File uploadDir = new File(bDir + path);
        if (!uploadDir.exists() && !uploadDir.mkdirs())
            throw new SimplifiedException(ErrorCode.IO_FAIL, "创建文件夹失败");
        String fullName = bDir + storeName + extention;
        File storedFile = new File(fullName);
        if (compress) {
            ZipOutputStream zipOutStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fullName)));
            zipOutStream.putNextEntry(new ZipEntry(fileName));
            FileCopyUtils.copy(file.getInputStream(), zipOutStream);
            zipOutStream.close();
        } else
            file.transferTo(storedFile);
        UploadResult rs = new UploadResult();
        rs.setFullName(fullName);
        rs.setStoreName(storeName + extention);
        rs.setSize(storedFile.length());
        rs.setExtention(extention);
        rs.setContentType("application/octet-stream");
        rs.setUploadTime(LocalDateTime.now());
        return rs;
    }

    /**
     * 批量上传文件
     */
    public static List<UploadResult> upload(List<MultipartFile> files, boolean compress, String... baseDir) {
        List<UploadResult> result = new ArrayList<>();
        if (null == files || 0 == files.size()) {
            return result;
        }
        for (MultipartFile file : files) {
            try {
                UploadResult uploadResult = upload(file, compress, baseDir);
                result.add(uploadResult);
            } catch (Exception e) {
                result.add(null);
            }
        }
        return result;
    }

    /**
     * 将指定URL的图片保存至服务器端
     */
    public static UploadResult saveFileFromUrl(String url) {
        UploadResult rs = new UploadResult();
        String storeName = generateFileName();
        String path = storeName.substring(0, storeName.indexOf("/") + 1);
        File uploadDir = new File(EnvCache.FileUploadDir + path);
        Map<String, String> downloadRs;
        if (!uploadDir.exists() && !uploadDir.mkdirs())
            throw new SimplifiedException(ErrorCode.IO_FAIL, "创建文件夹失败");
        try {
            downloadRs = HttpUtils.download(url, EnvCache.FileUploadDir + storeName);
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "下载文件错误", e);
        }
        rs.setFullName(EnvCache.FileUploadDir + storeName + downloadRs.get("extention"));
        rs.setStoreName(storeName + downloadRs.get("extention"));
        rs.setSize(Long.parseLong(downloadRs.get("bytes")));
        rs.setUploadTime(LocalDateTime.now());
        return rs;
    }

    /**
     * 生成统一格式的文件名
     */
    private static String generateFileName() {
        return dateFormat.format(LocalDateTime.now()) + RandomStringUtils.randomAlphanumeric(8);
    }

    public static class UploadResult {
        private String fullName;
        private String storeName;
        private Long size;
        private String extention;
        private String contentType;
        private LocalDateTime uploadTime;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public String getExtention() {
            return extention;
        }

        public void setExtention(String extention) {
            this.extention = extention;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public LocalDateTime getUploadTime() {
            return uploadTime;
        }

        public void setUploadTime(LocalDateTime uploadTime) {
            this.uploadTime = uploadTime;
        }
    }
}
