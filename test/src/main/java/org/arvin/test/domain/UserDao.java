package org.arvin.test.domain;

import org.spin.jpa.core.ARepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserDao extends ARepository<User, Long> {

}
