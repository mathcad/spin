package org.spin.data.sql.loader;

import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * 文件系统SQL装载器
 * <p>实现了基于文件系统的SQL装载通用方法</p>
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 */
public abstract class FileSystemSQLLoader extends GenericSqlLoader {

    private volatile boolean absolutePath = true;
    private final Object lock = new Object();

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
     * 根据ID(路径)读取文件
     *
     * @param id SQL的id
     * @return SQL文件
     */
    protected File getFile(String id) {
        String cmdFileName = id.substring(0, id.lastIndexOf('.'));
        String pathDbSep = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + fileDelimiter)) + this.getDbType().getProductName() + fileDelimiter + cmdFileName + getExtension();
        String path = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + fileDelimiter)) + cmdFileName + getExtension();
        try {
            if (absolutePath) {
                File file = new File(pathDbSep);
                if (file.exists()) {
                    return file;
                } else {
                    file = new File(path);
                    if (file.exists()) {
                        return file;
                    } else {
                        URL url = this.getClass().getResource(pathDbSep);
                        if (null == url) {
                            url = this.getClass().getResource(path);
                        }
                        if (null == url) {
                            throw new FileNotFoundException("sql模板文件不存在");
                        }
                        synchronized (lock) {
                            absolutePath = false;
                        }
                        return new File(url.getPath());
                    }
                }
            } else {
                URL url = this.getClass().getResource(pathDbSep);
                if (null == url) {
                    url = this.getClass().getResource(path);
                }
                if (null == url) {
                    File file = new File(pathDbSep);
                    if (file.exists()) {
                        synchronized (lock) {
                            absolutePath = true;
                        }
                        return file;
                    } else {
                        file = new File(path);
                        if (file.exists()) {
                            synchronized (lock) {
                                absolutePath = true;
                            }
                            return file;
                        } else {
                            throw new FileNotFoundException("sql模板文件不存在");
                        }
                    }
                }
                return new File(url.getPath());
            }
        } catch (Exception e) {
            throw new SimplifiedException("加载sql模板文件异常:[" + pathDbSep + "]", e);
        }


    }

    protected abstract String getExtension();
}
