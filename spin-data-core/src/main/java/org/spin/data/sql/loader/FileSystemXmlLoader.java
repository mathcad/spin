package org.spin.data.sql.loader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.spin.data.throwable.SQLError;
import org.spin.data.throwable.SQLException;

import java.io.File;
import java.util.List;

/**
 * 基于xml格式的sql装载器(从文件系统加载)
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 * @version 1.0
 */
public class FileSystemXmlLoader extends FileSystemSQLLoader {
    private final SAXReader reader = new SAXReader();

    @Override
    public String getSqlTemplateSrc(String id) {
        // 检查缓存
        if (this.useCache && this.sqlSourceMap.containsKey(id) && (!this.autoCheck || !this.isModified(id)))
            return this.sqlSourceMap.get(id);

        // 物理读取
        String cmdFileName = id.substring(0, id.lastIndexOf('.'));
        File sqlFile = this.getFile(id);
        Long version = sqlFile.lastModified();
        try {
            Document document = reader.read(sqlFile);
            Element root = document.getRootElement();
            List nodes = root.elements("sql");
            for (Object node : nodes) {
                Element elm = (Element) node;
                String sqlName = elm.attribute("id").getValue();
                String sql = elm.getText();
                this.sqlSourceMap.put(cmdFileName + "." + sqlName, sql);
                this.sqlSourceVersion.put(cmdFileName + "." + sqlName, version);
            }
        } catch (DocumentException e) {
            throw new SQLException(SQLError.CANNOT_GET_SQL, "读取模板文件异常:" + sqlFile.getName());
        }

        if (!this.sqlSourceMap.containsKey(id))
            throw new SQLException(SQLError.CANNOT_GET_SQL, "模板[" + sqlFile.getName() + "]中未找到指定ID的SQL:" + id);
        return this.sqlSourceMap.get(id);
    }

    @Override
    protected String getExtension() {
        return ".xml";
    }
}
