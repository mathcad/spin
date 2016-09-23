package org.arvin.test.domain;

import org.hibernate.criterion.Restrictions;
import org.infrastructure.jpa.core.CriteriaParam;
import org.infrastructure.util.EntityUtils;
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
        CriteriaParam cp = new CriteriaParam();
        cp.addField("organ.code");
        cp.addField("organ.name");
        cp.addField("roles");
        cp.addCriterion(Restrictions.eq("id", 1L));
        return userDao.list(cp).get(0);
    }
}
