package com.shipping.internal

import com.shipping.domain.enums.FunctionTypeE
import com.shipping.domain.sys.Function
import org.spin.core.auth.RolePermission

import java.security.PrivateKey
import java.security.PublicKey
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * Created by xuweinan on 2017/8/30.
 *
 * @author xuweinan
 */
object InfoCache {
    val permissionCache: MutableMap<Long, RolePermission> = ConcurrentHashMap()
    val functionCache: MutableMap<Long, MutableMap<FunctionTypeE, MutableList<Function>>> = ConcurrentHashMap()

    /** RSA公钥  */
    var RSA_PUBKEY: PublicKey? = null

    /** RSA私钥  */
    var RSA_PRIKEY: PrivateKey? = null
}
