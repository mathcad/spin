package org.spin.data.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 文件
 * <p>Created by xuweinan on 2017/4/20.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_file")
public class File extends AbstractEntity<File> {
    private static final long serialVersionUID = 5524987141178520509L;

    /**
     * 全局唯一id
     */
    @Column(length = 128, unique = true)
    private String guid;

    /**
     * 文件名
     */
    @Column
    private String fileName;

    /**
     * 原始文件名
     */
    @Column
    private String originName;

    /**
     * 文件存放路径
     */
    @Column(unique = true)
    private String filePath;

    /**
     * 扩展名
     */
    @Column(length = 16)
    private String extension;

    /**
     * 文件大小
     */
    @Column
    private Long size;

    /**
     * 扩展属性
     */
    @Column
    private String extAttr;

    /**
     * 是否私有
     */
    @Column
    private boolean priv = false;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getExtAttr() {
        return extAttr;
    }

    public void setExtAttr(String extAttr) {
        this.extAttr = extAttr;
    }

    public boolean isPriv() {
        return priv;
    }

    public void setPriv(boolean priv) {
        this.priv = priv;
    }
}
