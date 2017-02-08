package org.spin.sys;

import org.spin.security.Hex;
import org.spin.throwable.SimplifiedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinna
 */
public abstract class FileType {

    /**
     * 文本文件类型
     */
    public static final class Text extends FileType {
        public static final Text PLAIN = new Text(".txt", "text/plain", "");
        public static final Text JSON = new Text(".json", "application/json", "");
        public static final Text XML = new Text(".xml", "text/xml", "");
        public static final Text HTML = new Text(".html", "text/html", "");
        public static final Text CSS = new Text(".css", "text/css", "");

        private String extension;
        private String contentType;
        private String trait;

        private Text(String extension, String contentType, String trait) {
            this.extension = extension;
            this.contentType = contentType;
            this.trait = trait;
        }

        @Override
        public String getExtension() {
            return extension;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getTrait() {
            return trait;
        }
    }

    /**
     * 图像文件类型
     */
    public static final class Image extends FileType {
        public static final Image JPG = new Image(".jgp", "image/jpeg", "FFD8FF");
        public static final Image JPEG = new Image(".jpeg", "image/jpeg", "FFD8F");
        public static final Image BMP = new Image(".bmp", "application/x-MS-bmp", "424D");
        public static final Image PNG = new Image(".png", "image/png", "89504E47");
        public static final Image GIG = new Image(".gif", "image/gif", "47494638");
        public static final Image TIFF = new Image(".tiff", "image/tiff", "49492A00");

        private String extension;
        private String contentType;
        private String trait;

        private Image(String extension, String contentType, String trait) {
            this.extension = extension;
            this.contentType = contentType;
            this.trait = trait;
        }

        @Override
        public String getExtension() {
            return extension;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getTrait() {
            return trait;
        }
    }

    /**
     * Excel文件类型
     */
    public static final class Excel extends FileType {
        /**
         * Excel 97-2003
         */
        public static final Excel XLS = new Excel(".xls", "application/vnd.ms-excel", "D0CF11E0");

        /**
         * Excel 2007
         */
        public static final Excel XLSX = new Excel(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "504B030414");

        private String extension;
        private String contentType;
        private String trait;

        private Excel(String extension, String contentType, String trait) {
            this.extension = extension;
            this.contentType = contentType;
            this.trait = trait;
        }

        @Override
        public String getExtension() {
            return extension;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getTrait() {
            return trait;
        }
    }

    public static final class Toolkit {
        private static final Map<String, FileType> traits = new HashMap<>();

        static {
            traits.put("D0CF11E0", Excel.XLS);
            traits.put("504B030414", Excel.XLSX);
            traits.put("FFD8FF", Image.JPG);
            traits.put("424D", Image.BMP);
            traits.put("89504E47", Image.PNG);
            traits.put("47494638", Image.GIG);
            traits.put("49492A00", Image.TIFF);
        }

        /**
         * 检测文件类型
         *
         * @param file 待检测的文件
         * @return 文件类型，如果不支持，则返回null
         */
        public static FileType detectFileType(File file) {
            try {
                return detectFileType(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new SimplifiedException(ErrorAndExceptionCode.IO_FAIL, "文件不存在");
            }
        }

        /**
         * 检测文件类型
         *
         * @param inputStream 待检测的文件二进制流
         * @return 文件类型，如果不支持，则返回null
         */
        public static FileType detectFileType(InputStream inputStream) {
            byte[] trait = new byte[16];
            try {
                int total = inputStream.read(trait, 0, 16);
                if (total == -1)
                    throw new SimplifiedException(ErrorAndExceptionCode.IO_FAIL, "END OF FILE");
                else if (total == 0)
                    return Text.PLAIN;
                else
                    return detectFileType(trait);
            } catch (IOException e) {
                throw new SimplifiedException(ErrorAndExceptionCode.IO_FAIL, "流读取错误");
            }
        }

        /**
         * 检测文件类型
         *
         * @param trait 特征码(文件的前16个字节)
         * @return 文件类型，如果不支持，则返回null
         */
        public static FileType detectFileType(byte[] trait) {
            String traitStr = Hex.encodeHexStringU(trait);
            for (Map.Entry<String, FileType> t : traits.entrySet()) {
                if (traitStr.startsWith(t.getKey()))
                    return t.getValue();
            }
            return null;
        }
    }


    public abstract String getExtension();

    public abstract String getContentType();

    public abstract String getTrait();
}