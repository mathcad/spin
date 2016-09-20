package org.arvin.test.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Arvin on 2016/9/18.
 */
@Service
//@Transactional
public class UserService {
    @Autowired
    UserDao userDao;

    public User getUser() {
        return userDao.get(1L);
    }
}
