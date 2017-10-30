package com.shipping.controller.api;

import com.shipping.domain.sys.User;
import com.shipping.service.UserService;
import org.spin.web.RestfulResponse;
import org.spin.web.annotation.RestfulApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @RestfulApi("userInfo")
    public RestfulResponse userInfo() {
        Map<String, Object> user = userService.getCurrentUser();
        return RestfulResponse.ok(user);
    }



    @RestfulApi(auth = false, name = "list", path = "list", method = RequestMethod.POST)
    public RestfulResponse list() {

        List<User> users = new ArrayList<>();

        for(int i = 0; i < 100; i++) {
            User user = new User();

//            user.setName("test" + i);
            user.setNickname("测试" + i);
            user.setCreateTime(LocalDateTime.now());

            users.add(user);
        }


        return RestfulResponse.ok(users);
    }

    @RestfulApi(auth = false, name = "addUpdate", path = "addUpdate", method = RequestMethod.POST)
    public RestfulResponse addUpdate() {


        return RestfulResponse.ok();
    }


}
