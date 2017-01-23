package org.arvin.test;

import org.infrastructure.redis.RedisCacheSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arvin on 2017/1/23.
 */
@RestController
@RequestMapping("/")
public class TestController {
    @Autowired
    private RedisCacheSupport<Map<String, String>> cacheSupport;

    @RequestMapping("test")
    public Object test(String s) {
        Map<String, String> map = new HashMap<>();
        map.put("name", s);
        cacheSupport.put("user", map);
        cacheSupport.updateHashValue("users","a", map);
        return cacheSupport.getHashValue("users", "a");
    }
}
