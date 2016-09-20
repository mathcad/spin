package org.arvin.test.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Arvin on 2016/9/18.
 */
@RestController("/")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public User index() {
//        return userDao.listBySql("user.select", new java.util.HashMap<>()).get(0);
        return userService.getUser();
    }
}
