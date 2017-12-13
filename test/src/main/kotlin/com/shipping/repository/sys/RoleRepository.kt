package com.shipping.repository.sys

import com.shipping.domain.sys.Role
import org.spin.data.core.ARepository
import org.springframework.stereotype.Repository

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
class RoleRepository : ARepository<Role, Long>()
