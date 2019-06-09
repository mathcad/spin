package org.spin.common.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis通用工具类
 * 请选择键值时注意key分组 分组以树形结构展现 -&gt;
 * 分组样例  redis:shangli:service
 * @author YIJIUE
 */
public class RedisUtil<T> {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取字符串型的键 值
     * @param key 对象键
     * @return String
     */
    public String getValue(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 获取list集合的数据
     * @param key 键
     * @return List
     */
    public List<Object> getList(String key){
        String value = stringRedisTemplate.opsForValue().get(key);
        return JSON.parseArray(value, Object.class);
    }

    /**
     * 获取map类型的键 值
     * @param key 对象键
     * @return Map
     */
    public Map<Object, Object> getMap(String key){
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取set类型的键 值
     * @param key 键
     * @return Set
     */
    public Set<String> getSetCollection(String key){
        return stringRedisTemplate.opsForSet().members(key);
    }

    /**
     * 设置字符串型 键值对
     * 同时支持对象类型
     * @param key 键
     * @param value 值
     */
    public void setString(String key, T value){
        String s = JSON.toJSONString(value);
        stringRedisTemplate.opsForValue().set(key, s);
    }

    /**
     * 如果键不存在 则进行放入
     * @param key 键
     * @param value 值
     * @param time 时间
     * @param timeUnit 时间单位
     * @return 布尔值
     */
    public Boolean setIfNotExist(String key, T value, long time, TimeUnit timeUnit){
        String s = JSON.toJSONString(value);
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(key, s, time, timeUnit);
        return aBoolean;
    }

    /**
     * 设置String类型键值对 带过期时间
     * @param key 键
     * @param value 值
     * @param time 时间
     * @param timeUnit 时间单位
     */
    public void setString(String key, String value, long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    /**
     * 设置map型存储类型
     * @param key 键
     * @param map 值
     */
    public void setHashMap(String key, Map<String, Object> map){
        stringRedisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 设置list型存储类型
     * @param key 键
     * @param list 值
     */
    public void setList(String key, List<T> list){
        String value = JSON.toJSONString(list);
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置List存储对象 带过期时间单位
     * @param key 键
     * @param list 存储集合
     * @param time 时间
     * @param timeUnit 时间单位
     */
    public void setList(String key, List<T> list, long time, TimeUnit timeUnit){
        String value = JSON.toJSONString(list);
        stringRedisTemplate.opsForValue().set(key, value);
        this.setString(key, value, time, timeUnit);
    }

    /**
     * 设置set类型的存储类型
     * @param key 键
     * @param set 值
     * @return long
     */
    public long setSetCollection(String key, Set<T> set){
        return stringRedisTemplate.opsForSet().add(key, String.valueOf(set));
    }


    /**
     * 设置数字类型的键 值 自增长
     * @param key 键
     * @param incrmentNum 增长的数
     * @return long
     */
    public long increment(String key, int incrmentNum){
        Long increment = stringRedisTemplate.opsForValue().increment(key, incrmentNum);
        return incrmentNum;
    }


}
