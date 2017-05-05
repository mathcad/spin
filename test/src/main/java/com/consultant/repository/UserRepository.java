package com.consultant.repository;

import com.consultant.domain.sys.User;
import org.spin.data.core.ARepository;
import org.springframework.stereotype.Repository;

/**
 * <p>Created by xuweinan on 2017/4/18.</p>
 *
 * @author xuweinan
 */
@Repository
public class UserRepository extends ARepository<User, Long> {
}
