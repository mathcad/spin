package org.spin.data.sql;

import org.spin.data.core.DatabaseType;
import org.spin.data.core.PageRequest;
import org.spin.data.sql.resolver.TemplateResolver;

import java.util.Map;

/**
 * 拥有缓存机制的SQL装载器
 * <p>Created by xuweinan on 2016/8/13.</p>
 *
 * @author xuweinan
 * @version 1.1
 */
public interface SQLLoader {

    /**
     * 加载参数化的SQL语句
     *
     * @param id    sql的path
     * @param model 参数
     * @return 参数化的sql
     */
    SqlSource getSQL(String id, Map<String, ?> model);

    /**
     * 加载分页的参数化SQL语句
     *
     * @param dbType      数据库类型
     * @param id          sql的path
     * @param model       参数
     * @param pageRequest 分页参数
     * @return 参数化的sql
     */
    SqlSource getPagedSQL(DatabaseType dbType, String id, Map<String, ?> model, PageRequest pageRequest);

    /**
     * 加载SQL模板
     *
     * @param id sqlId
     * @return 模板字符串
     */
    String getSqlTemplateSrc(String id);

    /**
     * 判断一个SQL是否修改过
     *
     * @param id sqlId
     * @return 是否修改过
     */
    boolean isModified(String id);

    /**
     * 获取SQL加载器的模板解析器
     *
     * @return 模板解析器
     */
    TemplateResolver getTemplateResolver();

    /**
     * 设置SQL加载器的模板解析器
     *
     * @param resolver 模板解析器
     */
    void setTemplateResolver(TemplateResolver resolver);

    /**
     * SQL模板的根路径
     *
     * @return 模板根路径
     */
    String getRootUri();

    /**
     * 设置SQL模板的根路径
     *
     * @param rootUri 模板根路径
     */
    void setRootUri(String rootUri);

    /**
     * 是否每次都检测sql变化
     *
     * @return 是否检查
     */
    boolean isAutoCheck();

    /**
     * 设置是否检测SQL变化，开发模式下检查，生产环境不应检查
     *
     * @param check 是否检查
     */
    void setAutoCheck(boolean check);

    /**
     * 模板编码字符集
     *
     * @return 字符集
     */
    String getCharset();

    /**
     * 设置模板编码字符集
     *
     * @param charset 字符集
     */
    void setCharset(String charset);

    /**
     * 开启缓存机制
     *
     * @return 返回当前加载器本身
     */
    SQLLoader enableCache();

    /**
     * 关闭缓存机制
     *
     * @return 返回当前加载器本身
     */
    SQLLoader disableCache();
}
