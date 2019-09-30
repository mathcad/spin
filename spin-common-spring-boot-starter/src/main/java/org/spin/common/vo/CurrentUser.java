package org.spin.common.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.util.StringUtils;

/**
 * 当前用户
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class CurrentUser {
    private static final Logger logger = LoggerFactory.getLogger(CurrentUser.class);

    private final Long id;
    private final String name;
    private final TokenExpireType expireType;
    private final String sid;

    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private CurrentUser(Long id, String name, String expireType, String sid) {
        this.id = id;
        this.name = StringUtils.urlDecode(name);
        this.expireType = TokenExpireType.getByValue(expireType);
        this.sid = sid;
        Assert.notNull(id, "用户ID不能为空");
        Assert.notNull(name, "用户名称不能为空");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return id + ":" + StringUtils.urlEncode(name) + ":" + expireType.getValue() + ":" + sid;
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
            String user = StringUtils.urlDecode(from);
            String[] split = user.split(":");
            if (split.length != 4) {
                logger.warn("非法的用户信息: {}", user);
            }
            CurrentUser current = new CurrentUser(Long.parseLong(split[0]), split[1], split[2], split[3]);
            CURRENT.set(current);
            return current;
        } catch (Exception e) {
            logger.warn("非法的用户信息: {}", e.getMessage());
            return null;
        }
    }

    public TokenExpireType getExpireType() {
        return expireType;
    }

    public String getSid() {
        return sid;
    }

    /**
     * 清除线程上绑定的当前用户
     */
    public static void clearCurrent() {
        CURRENT.remove();
    }
}
