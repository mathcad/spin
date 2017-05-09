package org.spin.data.sql.loader;

import java.io.File;

/**
 * 文件系统SQL装载器
 * <p>实现了基于文件系统的SQL装载通用方法</p>
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 */
public abstract class FileSystemSQLLoader extends GenericSqlLoader {

    @Override
    public boolean isModified(String id) {
        File file = this.getFile(id);
        if (file == null)
            return true;
        long lastModify = file.lastModified();
        Long oldVersion = sqlSourceVersion.get(id);
        return oldVersion == null || oldVersion != lastModify;
    }

    /**
     * 根据ID与扩展名读取文件
     */
    protected abstract File getFile(String id);
}
