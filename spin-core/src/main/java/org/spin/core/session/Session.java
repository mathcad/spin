package org.spin.core.session;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Session定义
 * <p>Created by xuweinan on 2017/10/17.</p>
 *
 * @author xuweinan
 */
public interface Session {

    /**
     * 获取Session的Id
     *
     * @return Id
     */
    Serializable getId();

    /**
     * 设置Session的Id
     *
     * @param id Id
     */
    void setId(Serializable id);

    /**
     * Session的创建时间
     *
     * @return 创建时间
     */
    LocalDateTime getStartTimestamp();

    /**
     * Session的最后访问时间
     *
     * @return 最后访问时间
     */
    LocalDateTime getLastAccessTime();

    /**
     * 获取当前Session的超时时间，单位为毫秒
     *
     * @return 超时时间
     */
    long getTimeout();

    /**
     * 设置当前Session超时时间，单位为毫秒
     *
     * @param maxIdleTimeInMillis 超时时间
     */
    void setTimeout(long maxIdleTimeInMillis);

    /**
     * 获取当前Session用户的客户端主机名或IP地址
     *
     * @return 主机名或IP地址
     */
    String getHost();

    /**
     * 更新当前Session的最后访问时间（不修改任何属性，只是延长生命周期）
     */
    void touch();

    /**
     * 直接停止当前Session的生命周期
     */
    void stop();

    /**
     * 获取当前Session的所有属性名称
     *
     * @return 属性名称集合
     */
    Collection<Serializable> getAttributeKeys();

    /**
     * 获取指定key对应的属性
     *
     * @param key 属性名
     * @return 属性值
     */
    Serializable getAttribute(Serializable key);

    /**
     * 向Session中设置属性
     *
     * @param key   属性名
     * @param value 属性值
     */
    void setAttribute(Serializable key, Serializable value);

    /**
     * 从Session移除指定的键与对应的值
     *
     * @param key 属性名
     * @return 被移除的属性值
     */
    Serializable removeAttribute(Serializable key);

    /**
     * 判断当前Session是否有效
     *
     * @return 是否有效
     */
    boolean isValid();

    void validate();
}

