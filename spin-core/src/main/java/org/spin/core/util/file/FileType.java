package org.spin.core.util.file;

/**
 * 文件类型
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinna
 */
public abstract class FileType {
    /**
     * 文件扩展名，包含"."
     * 如<code>.exe</code>
     */
    protected String extension;

    /**
     * 文件格式
     * 如<code>"JPEG"</code>
     */
    protected String format;

    /**
     * 文件MIME类型
     */
    protected String contentType;

    /**
     * 二进制特征码
     */
    protected String trait;

    protected FileType(String extension, String contentType, String trait) {
        this.extension = extension.charAt(0) == '.' ? extension : "." + extension;
        this.format = this.extension.substring(1).toUpperCase();
        this.contentType = contentType;
        this.trait = trait.length() > 16 ? trait.toUpperCase().substring(0, 16) : trait.toUpperCase();
    }


    public String getExtension() {
        return extension;
    }


    public String getFormat() {
        return format;
    }


    public String getContentType() {
        return contentType;
    }


    public String getTrait() {
        return trait;
    }

    /**
     * 文本文件类型
     */
    public static final class Text extends FileType {
        public static final Text PLAIN = new Text(".txt", "text/plain", "");
        public static final Text JSON = new Text(".json", "application/json", "");
        public static final Text XML = new Text(".xml", "text/xml", "");
        public static final Text HTML = new Text(".html", "text/html", "");
        public static final Text CSS = new Text(".css", "text/css", "");

        public Text(String extension, String contentType, String trait) {
            super(extension, contentType, trait);
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
        public static final Image PSD = new Image(".psd", "", "");

        public Image(String extension, String contentType, String trait) {
            super(extension, contentType, trait);
        }
    }

    /**
     * 文档类型
     */
    public static final class Document extends FileType {
        public static final Document XLS = new Document(".xls", "application/vnd.ms-excel", "D0CF11E0");
        public static final Document XLSX = new Document(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "504B0304");

        public Document(String extension, String contentType, String trait) {
            super(extension, contentType, trait);
        }

    }

    /**
     * 压缩文件类型
     */
    public static final class Archive extends FileType {
        public static final Archive ZIP = new Archive(".zip", "", "");
        public static final Archive RAR = new Archive(".rar", "", "");
        public static final Archive Z7 = new Archive(".7z", "", "");
        public static final Archive GZIP = new Archive(".gz", "", "");
        public static final Archive TAR = new Archive(".tar", "", "");
        public static final Archive ARC = new Archive(".arc", "", "");

        public Archive(String extension, String contentType, String trait) {
            super(extension, contentType, trait);
        }
    }

    /**
     * 音频文件类型
     */
    public static final class Audio extends FileType {
        public static final Audio WAV = new Audio(".wav", "", "");

        public Audio(String extension, String contentType, String trait) {
            super(extension, contentType, trait);
        }
    }

    /**
     * 视频文件类型
     */
    public static final class Video extends FileType {
        public static final Video MP4 = new Video(".mp4", "", "");

        public Video(String extension, String contentType, String trait) {
            super(extension, contentType, trait);
        }
    }

    /**
     * 二进制文件类型
     */
    public static final class Bin extends FileType {
        public static final Video EXE = new Video(".exe", "", "");

        public Bin(String extension, String contentType, String trait) {
            super(extension, contentType, trait);
        }
    }
}
