package com.shipping.repository.sys

import com.shipping.domain.sys.Dict
import com.shipping.domain.sys.File
import com.shipping.domain.sys.Function
import com.shipping.domain.sys.FunctionGroup
import com.shipping.domain.sys.Organization
import com.shipping.domain.sys.Permission
import com.shipping.domain.sys.Region
import com.shipping.domain.sys.Role
import com.shipping.domain.sys.User
import org.spin.data.core.ARepository
import org.springframework.stereotype.Repository

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
open class DictRepository : ARepository<Dict, Long>()

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
open class FileRepository : ARepository<File, Long>()

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
open class FunctionGroupRepository : ARepository<FunctionGroup, Long>()

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
open class FunctionRepository : ARepository<Function, Long>()

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
open class OrganizatioinRepository : ARepository<Organization, Long>()

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
open class PermissionRepository : ARepository<Permission, Long>()

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
open class RegionRepository : ARepository<Region, Long>()

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
open class RoleRepository : ARepository<Role, Long>()

/**
 *
 * Created by xuweinan on 2017/7/18.
 *
 * @author xuweinan
 */
@Repository
open class UserRepository : ARepository<User, Long>()
