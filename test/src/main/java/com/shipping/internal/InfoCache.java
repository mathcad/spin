package com.shipping.internal;

import com.shipping.domain.enums.FunctionTypeE;
import com.shipping.domain.sys.Function;
import org.spin.core.auth.RolePermission;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Created by xuweinan on 2017/8/30.</p>
 *
 * @author xuweinan
 */
public final class InfoCache {
    public static final Map<Long, RolePermission> permissionCache = new ConcurrentHashMap<>();
    public static final Map<Long, Map<FunctionTypeE, List<Function>>> functionCache = new ConcurrentHashMap<>();

    /** RSA公钥 */
    public static PublicKey RSA_PUBKEY;

    /** RSA私钥 */
    public static PrivateKey RSA_PRIKEY;
}
