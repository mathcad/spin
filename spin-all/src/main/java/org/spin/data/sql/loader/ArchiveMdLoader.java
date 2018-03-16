package org.spin.data.sql.loader;

import org.spin.core.throwable.SQLException;
import org.spin.core.util.StringUtils;

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
    private static final String SPLITER = "===";
    private static final String REMARK = "//";

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
            while ((temp = StringUtils.trimTrailingWhitespace(bf.readLine())) != null) {
                if (temp.startsWith(SPLITER) || lastLine.startsWith(SPLITER)) {// 读取到===号，说明上一行是key，下面是注释或者SQL语句
                    if (list.size() != 1) {
                        throw new SQLException(SQLException.CANNOT_GET_SQL, "模板文件格式不正确:");
                    }
                    key = list.pollLast();
                    if (lastLine.startsWith(SPLITER) && !StringUtils.startsWithIgnoreBlank(temp, REMARK)) {
                        sql.append(temp).append("\n");
                    }
                    while ((tempNext = StringUtils.trimTrailingWhitespace(bf.readLine())) != null) {
                        if (StringUtils.isNotEmpty(tempNext)) {
                            if (tempNext.startsWith(SPLITER)) {
                                if (StringUtils.isEmpty(lastLine))
                                    throw new SQLException(SQLException.CANNOT_GET_SQL, "模板文件格式不正确:");
                                list.add(lastLine);
                                lastLine = tempNext;
                                this.sqlSourceMap.put(path + "." + key, sql.replace(sql.length() - 1, sql.length(), "").substring(0, sql.lastIndexOf("\n")));
                                this.sqlSourceVersion.put(path + "." + key, version);
                                sql = new StringBuilder();
                                break;
                            } else if (!StringUtils.startsWithIgnoreBlank(tempNext, REMARK)) {
                                sql.append(tempNext).append("\n");
                                lastLine = tempNext;
                            }
                        }
                    }
                } else if (StringUtils.isNotEmpty(temp) && !temp.startsWith(REMARK) && !temp.startsWith(SPLITER)) {
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

    @Override
    protected String getExtension() {
        return ".md";
    }
}
