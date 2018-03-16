package org.spin.data.sql.loader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.spin.core.throwable.SQLException;

import java.io.InputStream;
import java.util.List;

/**
 * 基于xml格式的sql装载器
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 * @version 1.0
 */
public class ArchiveXmlLoader extends ArchiveSQLLoader {
    private SAXReader reader = new SAXReader();

    @Override
    public String getSqlTemplateSrc(String id) {
        // 检查缓存
        if (this.useCache && this.sqlSourceMap.containsKey(id))
            return this.sqlSourceMap.get(id);

        // 物理读取
        String cmdFileName = id.substring(0, id.lastIndexOf('.'));
        InputStream sqlFile = this.getInputStream(id);
        Long version = 1L;
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
            throw new SQLException(SQLException.CANNOT_GET_SQL, "读取模板文件异常");
        }

        if (!this.sqlSourceMap.containsKey(id))
            throw new SQLException(SQLException.CANNOT_GET_SQL, "模板中未找到指定ID的SQL:" + id);
        return this.sqlSourceMap.get(id);
    }

    @Override
    protected String getExtension() {
        return ".xml";
    }
}
