package org.spin.jpa.core;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 基础用户
 * Created by xuweinan on 2017/4/18.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_user")
public class BaseUser extends AbstractUser {

    /**
     * 引用一个User
     */
    public static BaseUser ref(Long id) {
        BaseUser u = new BaseUser();
        u.setId(id);
        return u;
    }
}
