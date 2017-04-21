package com.consultant.controller;

import com.consultant.domain.enums.UserTypeE;
import com.consultant.domain.sys.User;
import com.consultant.service.UserService;
import org.spin.annotations.RestfulApi;
import org.spin.web.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>Created by xuweinan on 2016/10/13.</p>
 *
 * @author xuweinan
 */
@Controller
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RestfulApi(auth = false, path = "add")
    public RestfulResponse addUser() {
        User user = new User();
        user.setUserName("xuweinan");
        user.setMobile("123321");
        user.setUserType(UserTypeE.普通用户);
        user.setPassword("admin");
        userService.saveUser(user);
        return RestfulResponse.ok();
    }

    @RestfulApi(auth = false, path = "show")
    public RestfulResponse show() {
        User user = userService.getUser(1L);
        return RestfulResponse.ok(user);
    }
}
