package com.shipping.repository.sys

import com.shipping.domain.sys.User
import org.spin.data.core.ARepository
import org.springframework.stereotype.Repository

/**
 *
 * Created by xuweinan on 2017/7/18.
 *
 * @author xuweinan
 */
@Repository
class UserRepository : ARepository<User, Long>()
