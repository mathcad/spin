package org.arvin.test.dao;

import org.arvin.test.domain.sys.User;
import org.spin.jpa.core.ARepository;
import org.springframework.stereotype.Repository;

/**
 * Created by xuweinan on 2017/4/18.
 */
@Repository
public class UserDao extends ARepository<User, Long> {
}
