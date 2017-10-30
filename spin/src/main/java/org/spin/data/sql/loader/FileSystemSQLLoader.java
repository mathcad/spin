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
    protected File getFile(String id) {
        String cmdFileName = id.substring(0, id.lastIndexOf('.'));
        String path = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + "/")) + this.getDbType().getProductName() + "/" + cmdFileName + getExtension();
        URL url;
        try {
            url = this.getClass().getResource(path);
            if (null == url) {
                path = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + "/")) + cmdFileName + getExtension();
                url = this.getClass().getResource(path);
            }
            if (null == url) {
                File file = new File(path);
                if (file.exists()) {
                    return file;
                } else {
                    throw new FileNotFoundException("sql模板文件不存在");
                }
            }
        } catch (Exception e) {
            throw new SimplifiedException("加载sql模板文件异常:[" + path + "]", e);
        }
        return new File(url.getPath());
    }

    protected abstract String getExtension();
}
