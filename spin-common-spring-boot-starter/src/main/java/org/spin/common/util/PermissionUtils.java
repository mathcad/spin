package org.spin.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.service.remote.PermissionService;
import org.spin.core.util.DateUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * description
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/15.</p>
 */
public abstract class PermissionUtils {

    private static final Logger logger = LoggerFactory.getLogger(PermissionUtils.class);
    // 用户权限
    private static Map<Long, Set<String>> usersPermissions = new HashMap<>();

    private static long dueTime = System.currentTimeMillis();

    private static final Object lock = new Object();

    private static PermissionService permissionService;

    public static void init(PermissionService permissionService) {
        PermissionUtils.permissionService = permissionService;
    }

    private PermissionUtils() {
        throw new IllegalStateException("PermissionUtils class");
    }

    public static Set<String> getUserPermissions(long userId) {
        refreshPermission();
        return usersPermissions.get(userId);
    }

    private static void setPermissions(Map<Long, Set<String>> permissions) {
        usersPermissions = permissions;
    }

    private static void refreshPermission() {
        if (System.currentTimeMillis() - dueTime >= 0) {
            synchronized (lock) {
                if (System.currentTimeMillis() - dueTime >= 0) {
                    setPermissions(permissionService.getUserPermissions());
                    logger.info("refresh permission: {}", Thread.currentThread().getName());
                    dueTime = DateUtils.addMinutes(LocalDateTime.now(), 1).toInstant(ZoneOffset.of("+8")).toEpochMilli();
                }
            }
        }
    }
}
