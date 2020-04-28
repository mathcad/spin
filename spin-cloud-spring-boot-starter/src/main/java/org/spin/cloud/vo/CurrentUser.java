package org.spin.cloud.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.UtilClass;
import org.spin.cloud.throwable.BizException;
import org.spin.cloud.util.Env;
import org.spin.core.Assert;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.security.Base64;
import org.spin.core.session.SessionUser;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.DateUtils;
import org.spin.core.util.EnumUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.MapUtils;
import org.spin.core.util.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 当前用户
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public class CurrentUser extends SessionUser<Long> {
    private static final Logger logger = LoggerFactory.getLogger(CurrentUser.class);
    private static final TypeToken<Map<String, Set<String>>> STRING_SETSTR_MAP_TOKEN = new TypeToken<Map<String, Set<String>>>() {
    };

    private static final TypeToken<Set<RolePermission>> PERM_TYPE_TOKEN = new TypeToken<Set<RolePermission>>() {
    };

    private static final String REDIS_NOT_PREPARED = "CurrentUser未能顺利初始化, 无法访问Redis";
    private static final String REDIS_SESSION_KEY = "ALL_SESSION:";
    private static final Duration SESSION_TIME_OUT = Duration.ofHours(2L);
    private static final String SESSION_ENTERPRISE_REDIS_KEY = "SESSION_ENTERPRISE:";
    private static final String USER_ROLE_AND_GROUP_REDIS_KEY = "USER_ROLE_AND_GROUP:";
    private static final String SYS_ROLE_INFO_REDIS_KEY = "SYS_ROLE_INFO";
    private static final String SYS_ROLE_PERM_REDIS_KEY = "SYS_ROLE_PERMISSION";

    private static final String ENTERPRISE_DETP_CACHE_KEY = "ENTERPRISE_DETP_CACHE:";
    private static final String ENTERPRISE_STATION_CACHE_KEY = "ENTERPRISE_STATIONS_CACHE:";
    private static final String ENTERPRISE_CUSTOM_ORG_CACHE_KEY = "ENTERPRISE_CUSTOM_ORG_CACHE:";

    private static final String SUPER_AMIN_ROLE_CODE = "SUPER_ADMIN";
    private static final String FULL_SUPER_AMIN_ROLE_CODE = "0:SUPER_ADMIN";
    private static final String ENT_AMIN_ROLE_CODE = "ENT_ADMIN";

    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> ALL_CHILDREN_SCRIPT = new DefaultRedisScript<>();
    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> ALL_BROTHERS_SCRIPT = new DefaultRedisScript<>();
    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private static StringRedisTemplate redisTemplate;

    // region 内容
    private final long id;
    private final String name;
    private final TokenExpireType expireType;
    private final String sid;
    private final LocalDateTime loginTime;
    private final String loginIp;

    private final String originData;

    private SessionEmpInfo currentEmp = null;
    private Set<String> userRoleAndGroups = null;
    private Set<String> userActualRoles = null;
    private Set<RolePermission> allPermsInEnt = null;
    // endregion

    static {
        ALL_CHILDREN_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("luascript/getAllChildren.lua")));
        ALL_CHILDREN_SCRIPT.setResultType(List.class);

        ALL_BROTHERS_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("luascript/getBrother.lua")));
        ALL_BROTHERS_SCRIPT.setResultType(List.class);
    }

    private CurrentUser(String encodedFromStr) {
        try {
            String user = Base64.decodeWithUtf8(encodedFromStr);
            String[] split = user.split(":");
            if (split.length != 6) {
                logger.warn("非法的用户信息: {}", user);
                throw new BizException("非法的用户信息: " + user);
            }

            this.id = Long.parseLong(split[0]);
            this.name = StringUtils.trimToNull(StringUtils.urlDecode(split[1]));
            this.expireType = TokenExpireType.getByValue(split[2]);
            this.loginTime = DateUtils.toLocalDateTime(new Date(Long.parseLong(split[3])));
            this.loginIp = split[4];
            this.sid = split[5];
        } catch (Exception e) {
            logger.warn("非法的用户信息: {}", e.getMessage());
            throw new BizException("非法的用户信息", e);
        }


        this.originData = encodedFromStr;
        Assert.notNull(this.expireType, "会话过期方式不能为空");
        Assert.notNull(this.sid, "Session ID不能为空");
    }

    public static void init(StringRedisTemplate redisTemplate) {
        CurrentUser.redisTemplate = redisTemplate;
    }

    /**
     * 获取当前线程上绑定的用户
     *
     * @return 当前用户
     */
    public static CurrentUser getCurrent() {
        return CURRENT.get();
    }

    /**
     * 获取当前线程上绑定的用户, 如果不存在, 抛出异常
     *
     * @param msg 当前用户不存在时的出错提示,默认为"当前用户没有登录"
     * @return 当前用户
     */
    public static CurrentUser getCurrentNonNull(String... msg) {
        String nonMsg = msg == null || msg.length == 0 ? "当前用户没有登录" : msg[0];
        return Assert.notNull(CURRENT.get(), nonMsg);
    }

    /**
     * 清除线程上绑定的当前用户
     */
    public static void clearCurrent() {
        CURRENT.remove();
    }

    /**
     * 绑定指定用户到当前线程上
     *
     * @param current 当前用户
     */
    public static void setCurrent(CurrentUser current) {
        CURRENT.set(Assert.notNull(current, "当前用户不能为空"));
    }

    /**
     * 绑定指定用户到当前线程上
     *
     * @param from 当前用户
     * @return 当前线程上的用户
     */
    public static CurrentUser setCurrent(String from) {
        try {
            CurrentUser current = new CurrentUser(from);
            CURRENT.set(current);
            return current;
        } catch (Exception e) {
            logger.warn("非法的用户信息: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取用户当前Session上设置的员工企业信息
     * 如果非企业员工，返回的企业id与员工id均为0
     *
     * @return 员工企业信息
     */
    public static SessionEmpInfo getCurrentEmpInfo() {
        CurrentUser current = getCurrentNonNull();
        return current.getSessionEmpInfo();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取当前会话的过期方式
     *
     * @return 过期方式
     */
    public TokenExpireType getExpireType() {
        return expireType;
    }

    /**
     * 获取当前的Session ID
     *
     * @return Session ID
     */
    public String getSid() {
        return sid;
    }

    @Override
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    @Override
    public String getSessionId() {
        return sid;
    }

    @Override
    public String getLoginIp() {
        return loginIp;
    }

    /**
     * 获取用户当前Session上设置的员工企业信息
     * 如果非企业员工，返回(0, 0)
     *
     * @return 员工企业信息
     */
    public SessionEmpInfo getSessionEmpInfo() {
        if (null == currentEmp) {
            String s = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).opsForValue().get(SESSION_ENTERPRISE_REDIS_KEY + sid);
            if (null == s) {
                currentEmp = SessionEmpInfo.newNonEntUser(this.id);
            } else {
                currentEmp = JsonUtils.fromJson(s, SessionEmpInfo.class);
            }
        }

        return currentEmp;
    }

    /**
     * 用户是否是超级管理员
     *
     * @return 是/否
     */
    public boolean isSuperAdmin() {
        return getRoleAndGroups().contains(SUPER_AMIN_ROLE_CODE);
    }

    /**
     * 用户是否是企业管理员
     *
     * @return 是/否
     */
    public boolean isEnterpriseAdmin() {
        return getRoleAndGroups().contains(ENT_AMIN_ROLE_CODE);
    }

    /**
     * 用户在当前企业下的角色与用户组信息
     *
     * @return 角色与用户组边码列表(用户组编码以GROUP : 开头)
     */
    public Set<String> getRoleAndGroups() {
        SessionEmpInfo sessionEnterprise = getSessionEmpInfo();
        if (null != sessionEnterprise) {
            return getRoleAndGroups(sessionEnterprise.getEnterpriseId());
        }

        return Collections.emptySet();
    }

    /**
     * 用户在指定企业下的角色与用户组信息
     *
     * @param enterpriseId 企业id
     * @return 角色与用户组边码列表(用户组编码以 " GROUP : " 开头)
     */
    public Set<String> getRoleAndGroups(long enterpriseId) {
        String ent = Long.toString(enterpriseId);
        if (null == userRoleAndGroups) {
            String cache = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).opsForValue().get(USER_ROLE_AND_GROUP_REDIS_KEY + id);
            if (StringUtils.isNotEmpty(cache)) {
                userRoleAndGroups = StringUtils.splitToSet(cache, ",", StringUtils::trimToEmpty);
            } else {
                userRoleAndGroups = Collections.emptySet();
            }
        }

        Set<String> roleAndGroupsInEnt = new HashSet<>();
        for (String s : userRoleAndGroups) {
            if (s.startsWith(ent)) {
                roleAndGroupsInEnt.add(s.substring(ent.length() + 1));
            } else if (s.equals(FULL_SUPER_AMIN_ROLE_CODE)) {
                roleAndGroupsInEnt.add(SUPER_AMIN_ROLE_CODE.substring(2));
            }
        }
        return roleAndGroupsInEnt;
    }

    /**
     * 用户在当前企业下实际拥有的所有角色列表(解析用户组与用户继承关系)
     *
     * @return 角色列表(编码)
     */
    public Set<String> getActualRoles() {
        if (null == userActualRoles) {
            SessionEmpInfo sessionEnterprise = getSessionEmpInfo();
            if (null != sessionEnterprise) {
                userActualRoles = getActualRoles(sessionEnterprise.getEnterpriseId());
            }
        }

        return userActualRoles;
    }

    /**
     * 用户在指定企业下实际拥有的所有角色列表(解析用户组与用户继承关系)
     *
     * @param enterpriseId 企业id
     * @return 角色列表(编码)
     */
    public Set<String> getActualRoles(long enterpriseId) {
        Set<String> roleAndGroups = getRoleAndGroups(enterpriseId);

        return getActualRoles(roleAndGroups, Long.toString(enterpriseId));
    }

    /**
     * 判断用户在当前企业下是否拥有指定角色
     *
     * @param roleCode 角色编码
     * @return 是/否
     */
    public boolean hasRole(String roleCode) {
        SessionEmpInfo sessionEnterprise = getSessionEmpInfo();
        if (null != sessionEnterprise) {
            return hasRole(sessionEnterprise.getEnterpriseId(), roleCode);
        }
        return false;
    }

    /**
     * 判断用户在指定企业下是否拥有指定角色
     *
     * @param enterpriseId 企业id
     * @param roleCode     角色编码
     * @return 是/否
     */
    public boolean hasRole(long enterpriseId, String roleCode) {
        Set<String> roleAndGroups = getRoleAndGroups(enterpriseId);
        boolean contains = roleAndGroups.contains(roleCode);
        if (!contains) {
            return getActualRoles(roleAndGroups, Long.toString(enterpriseId)).contains(roleCode);
        }
        return true;
    }

    /**
     * 获取Session属性
     *
     * @param key 属性名
     * @return 属性值
     */
    public String getSessionAttr(String key) {
        return Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).<String, String>opsForHash().get(REDIS_SESSION_KEY + sid, key);
    }

    /**
     * 获取Session属性
     *
     * @param keys 属性名
     * @return 属性值
     */
    public Map<String, String> getSessionAttr(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return MapUtils.ofMap();
        }
        List<String> values = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).<String, String>opsForHash().multiGet(REDIS_SESSION_KEY + sid, keys);
        if (keys.size() != values.size()) {
            logger.error("获取Session属性异常, 结果不完整: {}", StringUtils.join(values, "\n"));
            throw new BizException("获取Session属性异常, 结果不完整");
        }
        Map<String, String> res = MapUtils.ofMap();
        for (int i = 0; i < values.size(); i++) {
            res.put(keys.get(i), values.get(i));
        }

        return res;
    }

    /**
     * 设置Session属性
     *
     * @param key   属性名(不能为空)
     * @param value 属性值
     */
    public void setSessionAttr(String key, String value) {
        Assert.notEmpty(key, "属性名不能为空");
        String sessionKey = REDIS_SESSION_KEY + sid;
        try {
            Boolean exists = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).hasKey(sessionKey);
            if (!Boolean.TRUE.equals(exists)) {
                redisTemplate.<String, String>opsForHash().put(sessionKey, key, value);
                redisTemplate.expire(sessionKey, SESSION_TIME_OUT.getSeconds(), TimeUnit.SECONDS);
            } else {
                redisTemplate.<String, String>opsForHash().put(sessionKey, key, value);
            }
        } catch (Exception e) {
            logger.warn("Redis 读取异常: ", e);
        }
    }

    /**
     * 设置Session属性
     *
     * @param attrs 属性Map
     */
    public void setSessionAttr(Map<String, String> attrs) {
        if (CollectionUtils.isEmpty(attrs)) {
            return;
        }

        String sessionKey = REDIS_SESSION_KEY + sid;
        try {
            Boolean exists = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).hasKey(sessionKey);
            if (!Boolean.TRUE.equals(exists)) {
                redisTemplate.<String, String>opsForHash().putAll(sessionKey, attrs);
                redisTemplate.expire(sessionKey, SESSION_TIME_OUT.getSeconds(), TimeUnit.SECONDS);
            } else {
                redisTemplate.<String, String>opsForHash().putAll(sessionKey, attrs);
            }
        } catch (Exception e) {
            logger.warn("Redis 读取异常: ", e);
        }
    }

    /**
     * 移除Sessio属性
     *
     * @param key 属性名
     */
    public void removeSessionAttr(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        String sessionKey = REDIS_SESSION_KEY + sid;
        Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).opsForHash().delete(sessionKey, key);
    }

    /**
     * 移除Sessio属性
     *
     * @param keys 属性列表
     */
    public void removeSessionAttr(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        String sessionKey = REDIS_SESSION_KEY + sid;
        Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).opsForHash().delete(sessionKey, keys.toArray());
    }

    /**
     * 获取一个用户在当前企业拥有的全部权限
     *
     * @return 权限列表
     */
    public Set<RolePermission> getAllPermissions() {
        if (null == allPermsInEnt) {
            Set<String> actualRoles = getActualRoles().stream().map(it -> getSessionEmpInfo().getEnterpriseId() + ":" + it).collect(Collectors.toSet());
            allPermsInEnt = redisTemplate.<String, String>opsForHash().multiGet(SYS_ROLE_PERM_REDIS_KEY, actualRoles).stream()
                .filter(StringUtils::isNotEmpty)
                .map(it -> Optional.ofNullable(JsonUtils.fromJson(it, PERM_TYPE_TOKEN)).orElse(Collections.emptySet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        }

        return allPermsInEnt;
    }

    /**
     * 获取一个用户在指定企业下拥有的全部权限
     *
     * @param enterpriseId 企业ID
     * @return 权限列表
     */
    public Set<RolePermission> getAllPermissions(long enterpriseId) {
        if (null != allPermsInEnt && enterpriseId == getSessionEmpInfo().getEnterpriseId()) {
            return allPermsInEnt;
        }

        Set<String> actualRoles = getActualRoles(enterpriseId).stream().map(it -> getSessionEmpInfo().getEnterpriseId() + ":" + it).collect(Collectors.toSet());
        return redisTemplate.<String, String>opsForHash().multiGet(SYS_ROLE_PERM_REDIS_KEY, actualRoles).stream()
            .filter(StringUtils::isNotEmpty)
            .map(it -> Optional.ofNullable(JsonUtils.fromJson(it, PERM_TYPE_TOKEN)).orElse(Collections.emptySet()))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    /**
     * 判断用户在当前企业是否拥有指定权限
     *
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    public boolean hasPermission(String permissionCode) {
        if (StringUtils.isEmpty(permissionCode)) {
            return false;
        }
        return getAllPermissions().stream().map(RolePermission::getPermissionCode).anyMatch(permissionCode::equals);
    }

    /**
     * 判断用户在制定企业下是否拥有指定权限
     *
     * @param permissionCode 权限编码
     * @param enterpriseId   企业ID
     * @return 是否拥有权限
     */
    public boolean hasPermission(String permissionCode, long enterpriseId) {
        if (StringUtils.isEmpty(permissionCode)) {
            return false;
        }
        return getAllPermissions(enterpriseId).stream().map(RolePermission::getPermissionCode).anyMatch(permissionCode::equals);
    }

    /**
     * 查询当前用户在当前接口上的数据权限
     *
     * @return 数据权限信息
     */
    public DataPermInfo getDataPermInfo() {
        String apiCode = Env.getCurrentApiCode();
        DataPermInfo info = new DataPermInfo();

        if (StringUtils.isEmpty(apiCode) || isSuperAdmin()) {
            info.setHasDataLimit(false);
            return info;
        }
        SessionEmpInfo sessionEnterprise = getSessionEmpInfo();

        DataLevel dataLevel = getAllPermissions().stream()
            .filter(it -> it.getPermissionCode().equals("DATA" + apiCode.substring(3)))
            .map(RolePermission::getAdditionalAttr)
            .map(Integer::parseInt)
            .min(Integer::compareTo)
            .map(it -> EnumUtils.getEnum(DataLevel.class, it))
            .orElse(DataLevel.ALL_DEPT);

        switch (dataLevel) {
            case ALL_DEPT:
            case ALL_STATION:
                info.setHasDataLimit(false);
                break;
            case CURRENT_LOWER:
                // 查询所有下级部门
                Set<Long> allChildren = getAllChildren(ENTERPRISE_DETP_CACHE_KEY, sessionEnterprise.getEnterpriseId(), sessionEnterprise.getDepts());
                info.setDeptIds(allChildren);
                CollectionUtils.mergeIntoLeft(info.getDeptIds(), sessionEnterprise.getDepts());
                if (CollectionUtils.isNotEmpty(info.getDeptIds())) {
                    info.setHimself(false);
                }
                break;
            case LOWER:
                // 查询所有下级部门
                allChildren = getAllChildren(ENTERPRISE_DETP_CACHE_KEY, sessionEnterprise.getEnterpriseId(), sessionEnterprise.getDepts());
                info.setDeptIds(allChildren);
                break;
            case CURRENT_DEPT:
                info.setDeptIds(sessionEnterprise.getDepts());
                if (CollectionUtils.isNotEmpty(info.getDeptIds())) {
                    info.setHimself(false);
                }
                break;
            case HIMSELF:
                info.setHimself(true);
                break;
            case COORDINATE_LOWER:
                // 查询同级岗位及所有下级岗位
                Set<Long> allBrothers = getAllBrothers(ENTERPRISE_STATION_CACHE_KEY, sessionEnterprise.getEnterpriseId(), sessionEnterprise.getStations());
                info.setStationIds(allBrothers);
                allChildren = getAllChildren(ENTERPRISE_STATION_CACHE_KEY, sessionEnterprise.getEnterpriseId(), sessionEnterprise.getDepts());
                CollectionUtils.mergeIntoLeft(info.getStationIds(), allChildren);
                if (CollectionUtils.isNotEmpty(info.getStationIds())) {
                    info.setHimself(false);
                }
                break;
            case COORDINATE:
                // 查询同级岗位
                allBrothers = getAllBrothers(ENTERPRISE_STATION_CACHE_KEY, sessionEnterprise.getEnterpriseId(), sessionEnterprise.getStations());
                info.setStationIds(allBrothers);
                if (CollectionUtils.isNotEmpty(info.getStationIds())) {
                    info.setHimself(false);
                }
                break;
            case CURRENT_STATION:
                info.setStationIds(sessionEnterprise.getStations());
                if (CollectionUtils.isNotEmpty(info.getStationIds())) {
                    info.setHimself(false);
                }
                break;
        }
        return info;
    }

    /**
     * 查询当前用户在指定的字段权限信息
     *
     * @param fieldPermCode 字段权限编码
     * @return 当前用户拥有的字段权限列表, null表示无需控制字段权限
     */
    public Set<String> getFieldPermInfo(String fieldPermCode) {
        if (StringUtils.isEmpty(fieldPermCode) || isSuperAdmin()) {
            return null;
        }

        return getAllPermissions().stream()
            .filter(it -> it.getPermissionCode().equals(fieldPermCode))
            .map(RolePermission::getAdditionalAttr)
            .filter(StringUtils::isNotBlank)
            .flatMap(it -> StringUtils.splitToSet(it, ",").stream()).collect(Collectors.toSet());
    }

    public static Set<String> getActualRoles(Set<String> roleAndGroups, String enterpriseId) {
        if (CollectionUtils.isEmpty(roleAndGroups)) {
            return Collections.emptySet();
        }

        List<String> res = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).<String, String>opsForHash().multiGet(SYS_ROLE_INFO_REDIS_KEY,
            CollectionUtils.ofArrayList("GROUP_ROLE", "ROLE_INHERITANCE"));

        // 角色-组合的的其他角色
        Map<String, Set<String>> roleInheritance = res.size() > 0 ? JsonUtils.fromJson(res.get(0), STRING_SETSTR_MAP_TOKEN) : Collections.emptyMap();

        // 用户组-组合的的其他角色
        Map<String, Set<String>> userGroupRole = res.size() > 1 ? JsonUtils.fromJson(res.get(1), STRING_SETSTR_MAP_TOKEN) : Collections.emptyMap();

        Set<String> userActualRoles = new HashSet<>();
        for (String roleOrGroup : roleAndGroups) {
            if (roleOrGroup.startsWith("GROUP:")) {
                Set<String> rs = userGroupRole.get(enterpriseId + ":" + roleOrGroup.substring(6));
                if (null != rs) {
                    for (String r : rs) {
                        userActualRoles.add(r.substring(enterpriseId.length() + 1));
                        Set<String> tmp = roleInheritance.get(r);
                        if (!CollectionUtils.isEmpty(tmp)) {
                            for (String s : tmp) {
                                userActualRoles.add(s.substring(enterpriseId.length() + 1));
                            }
                        }
                    }
                }
            } else {
                userActualRoles.add(roleOrGroup);
                String r = enterpriseId + ":" + roleOrGroup;
                Set<String> tmp = roleInheritance.get(r);
                if (!CollectionUtils.isEmpty(tmp)) {
                    for (String s : tmp) {
                        userActualRoles.add(s.substring(enterpriseId.length() + 1));
                    }
                }
            }
        }
        return userActualRoles;
    }

    private static Set<Long> getAllChildren(String rootKey, long enterpriseId, Set<Long> current) {
        return executeLua(rootKey, enterpriseId, current, ALL_CHILDREN_SCRIPT);
    }

    @SuppressWarnings("SameParameterValue")
    private static Set<Long> getAllBrothers(String rootKey, long enterpriseId, Set<Long> current) {
        return executeLua(rootKey, enterpriseId, current, ALL_BROTHERS_SCRIPT);
    }

    @SuppressWarnings("rawtypes")
    private static Set<Long> executeLua(String rootKey, long enterpriseId, Set<Long> current, DefaultRedisScript<List> script) {
        if (CollectionUtils.isNotEmpty(current)) {
            List<?> children = redisTemplate.execute(script, Collections.singletonList(rootKey + enterpriseId),
                current.stream().map(StringUtils::toString).toArray());
            return Optional.ofNullable(children).orElse(CollectionUtils.ofLinkedList()).stream().map(Object::toString)
                .map(d -> JsonUtils.fromJson(d, OrganVo.class)).map(OrganVo::getId).map(Long::valueOf).collect(Collectors.toSet());
        } else {
            return CollectionUtils.ofHashSet();
        }
    }

    @Override
    public String toString() {
        return originData;
    }
}
