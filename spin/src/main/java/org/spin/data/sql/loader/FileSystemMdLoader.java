package org.spin.data.sql.loader;

import org.spin.core.throwable.SQLException;
import org.spin.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * 基于Markdown语法的sql装载器(从文件系统加载)
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 */
public class FileSystemMdLoader extends FileSystemSQLLoader {
    @Override
    public String getSqlTemplateSrc(String id) {
        // 检查缓存
        if (this.useCache && this.sqlSourceMap.containsKey(id) && (!this.autoCheck || !this.isModified(id)))
            return this.sqlSourceMap.get(id);

        // 物理读取
        String path = id.substring(0, id.lastIndexOf('.'));
        File sqlFile = this.getFile(id);
        Long version = sqlFile.lastModified();
        LinkedList<String> list = new LinkedList<>();
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFile), charset))) {
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
            throw new SQLException(SQLException.CANNOT_GET_SQL, "读取模板文件异常:" + sqlFile.getName(), e);
        }
        if (!this.sqlSourceMap.containsKey(id))
            throw new SQLException(SQLException.CANNOT_GET_SQL, "模板[" + sqlFile.getName() + "]中未找到指定ID的SQL:" + id);
        return this.sqlSourceMap.get(id);
    }

    @Override
    protected String getExtension() {
        return ".md";
    }
}