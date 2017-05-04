package com.consultant.service;

import com.consultant.domain.sys.User;
import com.consultant.repository.UserRepository;
import org.spin.core.auth.Authenticator;
import org.spin.core.auth.RolePermission;
import org.spin.jpa.core.AbstractUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>Created by xuweinan on 2017/4/20.</p>
 *
 * @author xuweinan
 */
@Service
public class UserService implements Authenticator {

    @Autowired
    private UserRepository userDao;

    @Override
    public AbstractUser getSubject(Object o) {
        return null;
    }

    @Override
    public void preCheck(AbstractUser baseUser) {

    }

    @Override
    public RolePermission getRolePermissionList(Object o) {
        return null;
    }

    @Override
    public boolean authenticate(Object o, String s) {
        return true;
    }

    @Override
    public boolean checkAuthorities(Object o, String s) {
        return true;
    }

    @Override
    public void logAccess(Object o, Date date, String s) {

    }

    public User saveUser(User user) {
        return userDao.save(user);
    }

    public User getUser(Long id) {
        return userDao.get(id);
    }
}
