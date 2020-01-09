package org.spin.core.util.file;

import org.spin.core.util.CollectionUtils;

import java.util.List;

/**
 * 文件类型
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinna
 */
public abstract class FileType {
    /**
     * 合法文件扩展名列表，包含"."
     * 如<code>.exe</code>
     */
    protected List<String> validExts;

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

    /**
     * 特征码偏移
     */
    protected int traitOffset;

    protected FileType(String contentType, String trait, int traitOffset, String... validExts) {
        this.format = this.getClass().getSimpleName();
        this.contentType = contentType;
        this.trait = trait.length() > 16 ? trait.toUpperCase().substring(0, 16) : trait.toUpperCase();
        this.traitOffset = traitOffset;
        this.validExts = CollectionUtils.ofLinkedList(validExts);
    }

    public String getFirstExt() {
        return validExts.get(0);
    }

    public List<String> getValidExts() {
        return validExts;
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

    public int getTraitOffset() {
        return traitOffset;
    }

    /**
     * 文本文件类型
     */
    public static final class Text extends FileType {
        public static final Text PLAIN = new Text("text/plain", "", 0, ".txt");
        public static final Text JSON = new Text("application/json", "", 0, ".json");
        public static final Text XML = new Text("text/xml", "", 0, ".xml");
        public static final Text HTML = new Text("text/html", "", 0, ".html", ".htm");
        public static final Text CSS = new Text("text/css", "", 0, ".css");

        public Text(String contentType, String trait, int traitOffset, String... validExts) {
            super(contentType, trait, traitOffset, validExts);
        }
    }

    /**
     * 图像文件类型
     */
    public static final class Image extends FileType {
        public static final Image JPEG = new Image("image/jpeg", "FFD8F", 0, ".jpg", ".jpeg", ".jpe");
        public static final Image BMP = new Image("application/x-bmp", "424D", 0, ".bmp", ".rle", ".dib");
        public static final Image PNG = new Image("image/png", "89504E47", 0, ".png", "pns");
        public static final Image GIG = new Image("image/gif", "47494638", 0, ".gif");
        public static final Image TIFF = new Image("image/tiff", "49492A00", 0, ".tiff", ".tif");
        public static final Image ICO = new Image("image/x-icon", "00000100", 0, ".ico");
        public static final Image PSD = new Image("application/octet-stream", "38425053", 0, ".psd", ".pdd");
        public static final Image DICOM = new Image("application/octet-stream", "4449434D", 128, ".dcm", ".dc3", ".dic");
        public static final Image SCT = new Image("application/octet-stream", "4354", 80, ".sct", ".dc3", ".dic");

        public Image(String contentType, String trait, int traitOffset, String... validExts) {
            super(contentType, trait, traitOffset, validExts);
        }
    }

    /**
     * 文档类型
     */
    public static final class Document extends FileType {
        public static final Document PDF = new Document("application/pdf", "255044462D312E", 0, ".pdf");
        public static final Document XLS = new Document("application/vnd.ms-excel", "D0CF11E0", 0, ".xls");
        public static final Document XLSX = new Document("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "504B0304", 0, ".xlsx");

        public Document(String contentType, String trait, int traitOffset, String... validExts) {
            super(contentType, trait, traitOffset, validExts);
        }

    }

    /**
     * 压缩文件类型
     */
    public static final class Archive extends FileType {
        public static final Archive ZIP = new Archive("application/octet-stream", "504B0304", 0, ".zip");
        public static final Archive RAR = new Archive("application/octet-stream", "52617221", 0, ".rar");
        public static final Archive GZIP = new Archive("application/octet-stream", "1F8B", 0, ".gz");
        public static final Archive Z7 = new Archive("application/octet-stream", "377A", 0, ".7z");
        public static final Archive XZ = new Archive("application/octet-stream", "FD377A585A", 0, ".xz");
        public static final Archive BZ2 = new Archive("application/octet-stream", "425A", 0, ".bz2");
        public static final Archive CAB = new Archive("application/octet-stream", "49536328", 0, ".cab");

        public Archive(String contentType, String trait, int traitOffset, String... validExts) {
            super(contentType, trait, traitOffset, validExts);
        }
    }

    /**
     * 音频文件类型
     */
    public static final class Audio extends FileType {
        public static final Audio WAV = new Audio("audio/wav", "57415645", 0, ".wav");

        public Audio(String contentType, String trait, int traitOffset, String... validExts) {
            super(contentType, trait, traitOffset, validExts);
        }
    }

    /**
     * 视频文件类型
     */
    public static final class Video extends FileType {
        public static final Video MP4 = new Video("video/mpeg4", "667479706D703432", 4, ".mp4");

        public Video(String contentType, String trait, int traitOffset, String... validExts) {
            super(contentType, trait, traitOffset, validExts);
        }
    }

    /**
     * 二进制文件类型
     */
    public static final class Bin extends FileType {
        public static final Bin PE = new Bin("application/octet-stream", "4D5A", 0, ".exe", ".dll", ".sys");
        public static final Bin ELF = new Bin("application/octet-stream", "7F454C46", 0, "");

        public Bin(String contentType, String trait, int traitOffset, String... validExts) {
            super(contentType, trait, traitOffset, validExts);
        }
    }
}
