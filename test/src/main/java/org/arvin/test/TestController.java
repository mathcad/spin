package org.arvin.test;

import org.spin.annotations.RestfulApi;
import org.spin.cache.RedisCache;
import org.spin.web.RestfulResponse;
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
    private RedisCache<Map<String, String>> cacheSupport;

    @RestfulApi(auth = false, path = "test")
    public RestfulResponse test(String s) {
        Map<String, String> map = new HashMap<>();
        map.put("name", s);
        cacheSupport.put("user", map);
        cacheSupport.updateHashValue("users", "a", map);
        return RestfulResponse.ok(cacheSupport.getHashValue("users", "a"));
    }
}
