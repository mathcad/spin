package org.spin.core.util.file;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.HexUtils;
import org.spin.core.util.file.FileType.Document;
import org.spin.core.util.file.FileType.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 文件类型检测工具类
 * <p>Created by xuweinan on 2017/5/26.</p>
 *
 * @author xuweinan
 */
public abstract class FileTypeUtils {
    private static final List<Trait> traits = new ArrayList<>();
    private static final int TRAIT_LEN = 16;

    private FileTypeUtils() {
    }

    private static final class Trait {
        public final String ext;
        public final String traitBin;
        public final FileType type;

        public Trait(String ext, String traitBin, FileType type) {
            this.ext = ext;
            this.traitBin = traitBin;
            this.type = type;
        }
    }

    static {
        traits.add(new Trait(Document.XLS.getExtension(), Document.XLS.getTrait(), Document.XLS));
        traits.add(new Trait(Document.XLSX.getExtension(), Document.XLSX.getTrait(), Document.XLSX));
        traits.add(new Trait(Image.JPG.getExtension(), Image.JPG.getTrait(), Image.JPG));
        traits.add(new Trait(Image.BMP.getExtension(), Image.BMP.getTrait(), Image.BMP));
        traits.add(new Trait(Image.PNG.getExtension(), Image.PNG.getTrait(), Image.PNG));
        traits.add(new Trait(Image.GIG.getExtension(), Image.GIG.getTrait(), Image.GIG));
        traits.add(new Trait(Image.TIFF.getExtension(), Image.TIFF.getTrait(), Image.TIFF));
    }

    /**
     * 注册自定义类型
     *
     * @param types 自定义类型
     */
    public static void registType(Collection<FileType> types) {
        for (FileType f : types) {
            traits.add(new Trait(f.getExtension(), f.getTrait(), f));
        }
    }

    /**
     * 检测文件类型
     *
     * @param file 待检测的文件
     * @return 文件类型，如果不支持，则返回null
     */
    public static FileType detectFileType(File file) {
        String fileName = file.getName();
        int idx = fileName.lastIndexOf('.');
        String ext = idx > 0 ? fileName.substring(idx) : "";
        FileType ft = lookUp(ext, true);
        return null == ft ? detectFileTypeFromBin(file) : ft;
    }

    /**
     * 检测文件类型
     *
     * @param file 待检测的文件
     * @return 文件类型，如果不支持，则返回null
     */
    public static FileType detectFileTypeFromBin(File file) {
        byte[] trait = new byte[TRAIT_LEN];
        try (FileInputStream fis = new FileInputStream(file)) {
            int total = fis.read(trait, 0, TRAIT_LEN);
            if (total == -1)
                throw new SpinException(ErrorCode.IO_FAIL, "END OF FILE");
            else if (total == 0)
                return FileType.Text.PLAIN;
            else
                return detectFileType(trait);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "流读取错误");
        }
    }

    /**
     * 检测文件类型
     *
     * @param trait 特征码(文件的前16个字节)
     * @return 文件类型，如果不支持，则返回null
     */
    public static FileType detectFileType(byte[] trait) {
        byte[] t = trait.length > TRAIT_LEN ? Arrays.copyOf(trait, TRAIT_LEN) : trait;
        String traitStr = HexUtils.encodeHexStringU(t);
        return lookUp(traitStr, false);
    }

    private static FileType lookUp(String key, boolean isExt) {
        for (Trait t : traits) {
            if (isExt) {
                if (t.ext.equalsIgnoreCase(key))
                    return t.type;
            } else {
                if (key.startsWith(t.traitBin))
                    return t.type;
            }
        }
        return null;
    }
}
