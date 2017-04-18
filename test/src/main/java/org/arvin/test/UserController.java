package org.arvin.test;

import org.arvin.test.dao.UserDao;
import org.arvin.test.domain.enums.UserTypeE;
import org.arvin.test.domain.sys.User;
import org.spin.annotations.RestfulApi;
import org.spin.web.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by xuweinan on 2017/4/18.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserDao userDao;

    @RestfulApi(auth = false, path = "add")
    public RestfulResponse addUser() {
        User user = new User();
        user.setUserName("xuweinan");
        user.setMobile("123321");
        user.setUserType(UserTypeE.普通用户);
        user.setPassword("admin");
        userDao.save(user);
        return RestfulResponse.ok();
    }

    @RestfulApi(auth = false, path = "show")
    public RestfulResponse show() {
        User user = userDao.get(1L);
        return RestfulResponse.ok(user);
    }
}
