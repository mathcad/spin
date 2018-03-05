package org.spin.data.sql.loader;

import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;

import java.io.InputStream;

/**
 * 基于jar\war\ear包的SQL装载器
 * Created by xuweinan on 2016/10/15.
 *
 * @author xuweinan
 */
public abstract class ArchiveSQLLoader extends GenericSqlLoader {
    @Override
    public boolean isModified(String id) {
        return false;
    }

    protected InputStream getInputStream(String id) {
        String cmdFileName = id.substring(0, id.lastIndexOf('.'));
        String path = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + fileDelimiter)) + this.getDbType().getProductName() + fileDelimiter + cmdFileName + getExtension();
        try {
            InputStream is = this.getClass().getResourceAsStream(path);
            if (null == is) {
                path = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + fileDelimiter)) + cmdFileName + getExtension();
                is = this.getClass().getResourceAsStream(path);
            }
            return is;
        } catch (Exception e) {
            throw new SimplifiedException("加载sql模板文件异常:[" + path + "]", e);
        }
    }

    protected abstract String getExtension();
}
