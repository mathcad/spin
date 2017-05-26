package org.spin.core.util.file;

/**
 * 文件类型
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinna
 */
public interface FileType {

    /**
     * 文本文件类型
     */
    final class Text implements FileType {
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
    final class Image implements FileType {
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
    final class Document implements FileType {
        /**
         * Document 97-2003
         */
        public static final Document XLS = new Document(".xls", "application/vnd.ms-excel", "D0CF11E0");

        /**
         * Document 2007
         */
        public static final Document XLSX = new Document(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "504B030414");

        private String extension;
        private String contentType;
        private String trait;

        private Document(String extension, String contentType, String trait) {
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
     * 压缩文档类型
     */
    final class Archive implements FileType {
        /**
         * ZIP file
         */
        public static final Archive ZIP = new Archive(".zip", "application/vnd.ms-excel", "D0CF11E0");

        /**
         * RAR file
         */
        public static final Archive RAR = new Archive(".rar", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "504B030414");

        /**
         * 7-zip file
         */
        public static final Archive Z7 = new Archive(".7z", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "504B030414");

        private String extension;
        private String contentType;
        private String trait;

        private Archive(String extension, String contentType, String trait) {
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

    String getExtension();

    String getContentType();

    String getTrait();
}
