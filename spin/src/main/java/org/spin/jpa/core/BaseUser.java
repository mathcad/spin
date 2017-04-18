package org.spin.jpa.core;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 基础用户
 * <p>Created by xuweinan on 2017/4/18.</p>
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_user")
public class BaseUser extends AbstractUser {
    private static final long serialVersionUID = -4586086259312268641L;

    /**
     * 引用一个User
     */
    public static BaseUser ref(Long id) {
        BaseUser u = new BaseUser();
        u.setId(id);
        return u;
    }
}
