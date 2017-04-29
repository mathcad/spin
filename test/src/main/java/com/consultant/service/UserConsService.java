package com.consultant.service;

import com.consultant.domain.sys.UserCons;
import com.consultant.repository.UserConsRepository;
import org.spin.jpa.query.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>Created by xuweinan on 2017/4/28.</p>
 *
 * @author xuweinan
 */
@Service
public class UserConsService {

    @Autowired
    private UserConsRepository userConsDao;

    public Object test() {
        CriteriaBuilder cb = CriteriaBuilder.forClass(UserCons.class);
        cb.addFields("cons.user.createBy.userName");
        cb.addFields(CriteriaBuilder.ALL_COLUMNS);
        cb.eq("id", 1L);
        Object a = userConsDao.list(cb);
        return a;
    }
}
