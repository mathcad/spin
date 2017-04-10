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

package org.spin.jpa.sql.loader;

import org.spin.throwable.SQLException;
import org.spin.throwable.SimplifiedException;
import org.spin.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * 基于Markdown语法的sql装载器(从jar包中加载)
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 */
public class ArchiveMdLoader extends ArchiveSQLLoader {
    @Override
    public String getSqlTemplateSrc(String id) {
        // 检查缓存
        if (this.useCache && this.sqlSourceMap.containsKey(id))
            return this.sqlSourceMap.get(id);

        // 物理读取
        String path = id.substring(0, id.lastIndexOf('.'));
        InputStream sqlFile = this.getInputStream(id);
        Long version = 1L;
        LinkedList<String> list = new LinkedList<>();
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(sqlFile, charset))) {
            String temp;
            String tempNext;
            String lastLine = "";
            StringBuilder sql = new StringBuilder();
            String key = "";
            while ((temp = bf.readLine()) != null) {
                temp = StringUtils.trimTrailingWhitespace(temp);
                if (temp.startsWith("===") || lastLine.startsWith("===")) {// 读取到===号，说明上一行是key，下面是注释或者SQL语句
                    if (list.size() != 1)
                        throw new SQLException(SQLException.CANNOT_GET_SQL, "模板文件格式不正确:");
                    key = list.pollLast();
                    if (lastLine.startsWith("===") && !StringUtils.trimLeadingWhitespace(temp).startsWith("//"))
                        sql.append(temp).append("\n");
                    while ((tempNext = bf.readLine()) != null) {
                        if (StringUtils.isNotBlank(tempNext)) {
                            tempNext = StringUtils.trimTrailingWhitespace(tempNext);
                            if (tempNext.startsWith("===")) {
                                if (StringUtils.isEmpty(lastLine))
                                    throw new SQLException(SQLException.CANNOT_GET_SQL, "模板文件格式不正确:");
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
            throw new SQLException(SQLException.CANNOT_GET_SQL, "读取模板文件异常:", e);
        }
        if (!this.sqlSourceMap.containsKey(id))
            throw new SQLException(SQLException.CANNOT_GET_SQL, "模板中未找到指定ID的SQL:" + id);
        return this.sqlSourceMap.get(id);
    }

    protected InputStream getInputStream(String id) {
        String cmdFileName = id.substring(0, id.lastIndexOf('.'));
        String path = (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + "/")) + cmdFileName + ".md";
        try {
            return this.getClass().getResourceAsStream(path);
        } catch (Exception e) {
            throw new SimplifiedException("加载sql模板文件异常:[" + path + "]", e);
        }
    }
}
