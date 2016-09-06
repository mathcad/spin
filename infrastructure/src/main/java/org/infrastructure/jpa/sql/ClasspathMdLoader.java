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
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * 基于Markdown语法的sql装载器
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 */
public class ClasspathMdLoader extends FileSystemSQLLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClasspathMdLoader.class);

    @Override
    public String getSqlTemplateSrc(String id) {
        // 检查缓存
        if (this.use_cache && this.sqlSourceMap.containsKey(id) && (!this.autoCheck || !this.isModified(id)))
            return this.sqlSourceMap.get(id);

        // 物理读取
        String path = id.substring(0, id.lastIndexOf('.'));
        File sqlFile = this.getFile(id);
        Long version = sqlFile.lastModified();
        LinkedList<String> list = new LinkedList<>();
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFile), charset));
            String temp;
            String tempNext;
            String lastLine = "";
            StringBuilder sql = new StringBuilder();
            String key = "";
            while ((temp = bf.readLine()) != null) {
                temp = StringUtils.trimTrailingWhitespace(temp);
                if (temp.startsWith("===") || lastLine.startsWith("===")) {// 读取到===号，说明上一行是key，下面是注释或者SQL语句
                    if (list.size() != 1)
                        throw new SQLException(SQLException.CANNOT_GET_SQL, "模板文件格式不正确:" + sqlFile.getName());
                    key = list.pollLast();
                    if (lastLine.startsWith("===") && !StringUtils.trimLeadingWhitespace(temp).startsWith("//"))
                        sql.append(temp).append("\n");
                    while ((tempNext = bf.readLine()) != null) {
                        if (StringUtils.isNotBlank(tempNext)) {
                            tempNext = StringUtils.trimTrailingWhitespace(tempNext);
                            if (tempNext.startsWith("===")) {
                                if (StringUtils.isEmpty(lastLine))
                                    throw new SQLException(SQLException.CANNOT_GET_SQL, "模板文件格式不正确:" + sqlFile.getName());
                                list.add(lastLine);
                                lastLine = tempNext;
                                this.sqlSourceMap.put(path + "." + key, sql.replace(sql.length() - 1, sql.length(), "").substring(0, sql.lastIndexOf("\n")));
                                this.sqlSourceVersion.put(path + "." + key, version);
                                sql = new StringBuilder();
                                break;
                            } else if (!StringUtils.trimLeadingWhitespace(tempNext).startsWith("//")) {
                                sql.append(tempNext).append("\n");
                                lastLine = tempNext;
                            }
                        }
                    }
                } else if (!StringUtils.isBlank(temp) && !temp.startsWith("//") && !temp.startsWith("===")) {
                    list.add(temp);
                }
            }
            this.sqlSourceMap.put(path + "." + key, sql.substring(0, sql.lastIndexOf("\n")));
            this.sqlSourceVersion.put(path + "." + key, version);
        } catch (IOException e) {
            throw new SQLException(SQLException.CANNOT_GET_SQL, "解析模板文件异常:" + sqlFile.getName(), e);
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        if (!this.sqlSourceMap.containsKey(id))
            throw new SQLException(SQLException.CANNOT_GET_SQL, "模板[" + sqlFile.getName() + "]中未找到指定ID的SQL:" + id);
        return this.sqlSourceMap.get(id);
    }

    @Override
    protected File getFile(String id) {
        String cmdFileName = id.substring(0, id.lastIndexOf("."));
        String path = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + "/")) + cmdFileName + ".md";
        String uri;
        uri = this.getClass().getResource(path).getPath();
        return new File(uri);
    }
}