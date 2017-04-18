package org.spin.jpa.sql;

/**
 * 定义SQL语句
 * <p>Created by xuweinan on 2016/9/24.</p>
 *
 * @author xuweinan
 */
public class SQLSource {

    private String id;
    private String template;
    private int line = 0;
    //数据库插入用
    private int idType;

    public SQLSource() {
    }

    public SQLSource(String id, String template) {
        this.id = id;
        this.template = template;
    }

    public SQLSource(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public int getIdType() {
        return idType;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

}
