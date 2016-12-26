package org.arvin.test.domain;

import org.hibernate.criterion.Restrictions;
import org.infrastructure.jpa.query.DetachedCriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Arvin on 2016/9/18.
 */
@Service
public class UserService {
    @Autowired
    UserDao userDao;

    public User getUser() {
        DetachedCriteriaBuilder dc = DetachedCriteriaBuilder.forClass(User.class);
        dc.addField("organ.code","organ.name","roles");
        dc.addCriterion(Restrictions.eq("id", 1L));
        return userDao.list(dc).get(0);
    }
}
