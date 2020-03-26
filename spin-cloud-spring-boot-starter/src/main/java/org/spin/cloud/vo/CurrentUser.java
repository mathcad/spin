package org.spin.cloud.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.UtilClass;
import org.spin.cloud.throwable.BizException;
import org.spin.core.Assert;
import org.spin.core.collection.Pair;
import org.spin.core.collection.Triple;
import org.spin.core.collection.Tuple;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.security.Base64;
import org.spin.core.session.SessionUser;
import org.spin.core.util.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private static Pair<Long, Long> NON_ENTERPRISE = Tuple.of(0L, 0L);

    private static final String REDIS_NOT_PREPARED = "CurrentUser未能顺利初始化, 无法访问Redis";
    private static final String REDIS_SESSION_KEY = "ALL_SESSION:";
    private static final Duration SESSION_TIME_OUT = Duration.ofHours(2L);
    private static final String SESSION_ENTERPRISE_REDIS_KEY = "SESSION_ENTERPRISE:";
    private static final String USER_ROLE_AND_GROUP_REDIS_KEY = "USER_ROLE_AND_GROUP:";
    private static final String SYS_ROLE_INFO_REDIS_KEY = "SYS_ROLE_INFO";

    private static final String SUPER_AMIN_ROLE_CODE = "0:SUPER_ADMIN";
    private static StringRedisTemplate redisTemplate;

    private final long id;
    private final String name;
    private final TokenExpireType expireType;
    private final String sid;
    private final LocalDateTime loginTime;
    private final String loginIp;

    private final String originData;

    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

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
     * 绑定指定用户到当前线程上
     *
     * @param current 当前用户
     */
    public static void setCurrent(CurrentUser current) {
        CURRENT.set(Assert.notNull(current, "当前用户不能为空"));
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
     * 获取用户当前Session上设置的企业
     * 如果非企业员工，返回(用户ID, 0, 0)
     *
     * @return 当前用户ID, 当前员工ID, 当前企业ID
     */
    public static Triple<Long, Long, Long> getEnterpriseInfo() {
        CurrentUser current = getCurrentNonNull();
        Pair<Long, Long> enterprise = current.getSessionEnterprise();
        return Tuple.of(current.getId(), enterprise.c1, enterprise.c2);
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
     * 获取用户当前Session上设置的企业
     * 如果非企业员工，返回(0, 0)
     *
     * @return 当前员工ID, 当前企业ID
     */
    public Pair<Long, Long> getSessionEnterprise() {
        String s = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).opsForValue().get(SESSION_ENTERPRISE_REDIS_KEY + sid);
        if (null == s) {
            return NON_ENTERPRISE;
        }
        int i = s.indexOf(':');
        return Tuple.of(Long.parseLong(s.substring(0, i)), Long.parseLong(s.substring(i + 1)));
    }

    /**
     * 设置用户当前Session上的企业
     *
     * @param empId        员工id
     * @param enterpriseId 企业id
     * @param expireTime   过期时间
     * @param timeUnit     时间单位
     */
    public void setSessionEnterprise(long empId, long enterpriseId, long expireTime, TimeUnit timeUnit) {
        Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).opsForValue()
            .set(SESSION_ENTERPRISE_REDIS_KEY + sid, empId + ":" + enterpriseId, expireTime, timeUnit);
    }

    /**
     * 设置用户当前Session上的企业
     * <p>
     * 更新旧的当前企业，数据有效期不变，如果没有旧值，更新失败
     * </p>
     *
     * @param empId        员工id
     * @param enterpriseId 企业id
     */
    public void setSessionEnterprise(long empId, long enterpriseId) {
        Boolean hasKey = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).hasKey(SESSION_ENTERPRISE_REDIS_KEY + sid);
        if (null != hasKey && hasKey) {
            redisTemplate.opsForValue().set(SESSION_ENTERPRISE_REDIS_KEY + sid, empId + ":" + enterpriseId);
        } else {
            throw new BizException("用户当前会话未设置过当前企业");
        }
    }

    /**
     * 用户是否是超级管理员
     *
     * @return 是/否
     */
    public boolean isSuperAdmin() {
        Boolean exist = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).hasKey(USER_ROLE_AND_GROUP_REDIS_KEY + id);
        if (Boolean.TRUE.equals(exist)) {
            String cache = redisTemplate.opsForValue().get(USER_ROLE_AND_GROUP_REDIS_KEY + id);
            return Arrays.asList(StringUtils.trimToEmpty(cache).split(",")).contains(SUPER_AMIN_ROLE_CODE);
        }
        logger.warn("获取用户角色缓存失败,");
        return false;
    }

    /**
     * 用户在当前企业下的角色与用户组信息
     *
     * @return 角色与用户组边码列表(用户组编码以GROUP : 开头)
     */
    public Set<String> getRoleAndGroups() {
        Pair<Long, Long> sessionEnterprise = getSessionEnterprise();
        if (null != sessionEnterprise) {
            return getRoleAndGroups(sessionEnterprise.c2);
        }

        return Collections.emptySet();
    }

    /**
     * 用户在指定企业下的角色与用户组信息
     *
     * @param enterpriseId 企业id
     * @return 角色与用户组边码列表(用户组编码以GROUP : 开头)
     */
    public Set<String> getRoleAndGroups(long enterpriseId) {
        String ent = Long.toString(enterpriseId);
        String cache = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).opsForValue().get(USER_ROLE_AND_GROUP_REDIS_KEY);
        if (StringUtils.isNotEmpty(cache)) {
            Set<String> roleAndGroups = new HashSet<>(cache.length());
            String[] tmp = StringUtils.trimToEmpty(cache).split(",");
            for (String s : tmp) {
                if (s.startsWith("GROUP:")) {
                    if (s.startsWith("GROUP:" + ent + ":")) {
                        roleAndGroups.add(s.substring(7 + ent.length()));
                    }
                } else {
                    if (s.startsWith(ent + ":")) {
                        roleAndGroups.add(s.substring(ent.length() + 1));
                    } else if (s.equals(SUPER_AMIN_ROLE_CODE)) {
                        roleAndGroups.add(SUPER_AMIN_ROLE_CODE.substring(2));
                    }
                }
            }

            return roleAndGroups;
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * 用户在当前企业下实际拥有的所有角色列表(解析用户组与用户继承关系)
     *
     * @return 角色列表(编码)
     */
    public Set<String> getActualRoles() {
        Pair<Long, Long> sessionEnterprise = getSessionEnterprise();
        if (null != sessionEnterprise) {
            return getActualRoles(sessionEnterprise.c2);
        }

        return Collections.emptySet();
    }

    /**
     * 用户在指定企业下实际拥有的所有角色列表(解析用户组与用户继承关系)
     *
     * @param enterpriseId 企业id
     * @return 角色列表(编码)
     */
    public Set<String> getActualRoles(long enterpriseId) {
        Set<String> roleAndGroups = getRoleAndGroups(enterpriseId);

        return getActualRoles(roleAndGroups);
    }

    /**
     * 判断用户在当前企业下是否拥有指定角色
     *
     * @param roleCode 角色编码
     * @return 是/否
     */
    public boolean hasRole(String roleCode) {
        Pair<Long, Long> sessionEnterprise = getSessionEnterprise();
        if (null != sessionEnterprise) {
            return hasRole(sessionEnterprise.c2, roleCode);
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
            return getActualRoles(roleAndGroups).contains(roleCode);
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

    private Set<String> getActualRoles(Set<String> roleAndGroups) {
        if (CollectionUtils.isEmpty(roleAndGroups)) {
            return roleAndGroups;
        }

        List<String> res = Assert.notNull(redisTemplate, REDIS_NOT_PREPARED).<String, String>opsForHash().multiGet(SYS_ROLE_INFO_REDIS_KEY,
            CollectionUtils.ofArrayList("GROUP_ROLE", "ROLE_INHERITANCE"));

        // 角色-组合的的其他角色
        Map<String, Set<String>> roleInheritance = res.size() > 0 ? JsonUtils.fromJson(res.get(0), STRING_SETSTR_MAP_TOKEN) : Collections.emptyMap();

        // 用户组-组合的的其他角色
        Map<String, Set<String>> userGroupRole = res.size() > 1 ? JsonUtils.fromJson(res.get(1), STRING_SETSTR_MAP_TOKEN) : Collections.emptyMap();

        Set<String> actualRoles = new HashSet<>();

        roleAndGroups.forEach(it -> {
            if (it.startsWith("GROUP:")) {
                Set<String> rs = userGroupRole.get(it.substring(6));
                if (null != rs) {
                    for (String r : rs) {
                        actualRoles.add(r);
                        Set<String> tmp = roleInheritance.get(r);
                        if (!CollectionUtils.isEmpty(tmp)) {
                            actualRoles.addAll(tmp);
                        }
                    }
                }
            } else {
                actualRoles.add(it);
                Set<String> tmp = roleInheritance.get(it);
                if (!CollectionUtils.isEmpty(tmp)) {
                    actualRoles.addAll(tmp);
                }
            }
        });
        return actualRoles;
    }

    @Override
    public String toString() {
        return originData;
    }
}
