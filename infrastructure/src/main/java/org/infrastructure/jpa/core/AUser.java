package org.infrastructure.jpa.core;

import javax.persistence.Id;

/**
 * 基础类型，只映射到id、name字段
 *
 * @version V1.0
 */
public class AUser implements java.io.Serializable {
    private static final long serialVersionUID = -4170912618916172853L;

    @Id
    Long id;

    String name;

    String realName;

    /**
     * 引用一个User
     */
    public static AUser ref(Long id) {
        AUser u = new AUser();
        u.setId(id);
        return u;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
