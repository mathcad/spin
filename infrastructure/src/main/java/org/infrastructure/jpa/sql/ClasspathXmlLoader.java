/*
 *  Copyright 2002-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.infrastructure.jpa.sql;

import org.infrastructure.throwable.SQLException;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;

/**
 * 基于xml格式的sql装载器
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 * @version 1.0
 */
public class ClasspathXmlLoader extends FileSystemSQLLoader {
    private SAXReader reader = new SAXReader();

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
            throw new SQLException(SQLException.CANNOT_GET_SQL, "读取模板文件异常:" + sqlFile.getName());
        }

        if (!this.sqlSourceMap.containsKey(id))
            throw new SQLException(SQLException.CANNOT_GET_SQL, "模板[" + sqlFile.getName() + "]中未找到指定ID的SQL:" + id);
        return this.sqlSourceMap.get(id);
    }

    @Override
    protected File getFile(String id) {
        String cmdFileName = id.substring(0, id.lastIndexOf('.'));
        String path = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + "/")) + cmdFileName + ".xml";
        String uri;
        try {
            uri = this.getClass().getResource(path).getPath();
        } catch (Exception e) {
            throw new SimplifiedException("加载sql模板文件异常:[" + path + "]", e);
        }
        return new File(uri);
    }
}