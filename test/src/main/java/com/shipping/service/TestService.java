package com.shipping.service;

import com.shipping.domain.sys.User;
import com.shipping.repository.sys.UserRepository;
import org.spin.core.throwable.SimplifiedException;
import org.spin.web.annotation.RestfulMethod;
import org.spin.web.annotation.RestfulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Transient;
import java.util.List;

/**
 * <p>Created by xuweinan on 2017/9/17.</p>
 *
 * @author xuweinan
 */
@RestfulService("Test")
public class TestService {

    @Autowired
    private UserRepository userDao;

    @RestfulMethod(auth = false, authRouter = "a")
    public int aaa(String[] pp, List a, MultipartFile[] b) {
//        throw new SimplifiedException("aaa");
        return pp.length;
    }

    @Transactional
    @RestfulMethod(auth = false, value = "b")
    public void testTransaction() {
        User user = new User();
        user.setRealName("二傻子");
        user.setUserName("admin");
        user.setPassword("123");
        user.setMobile("13111111111");
        user.setEmail("none@qq.com");
        System.out.println(userDao.getCurrentDataSourceName());
        userDao.save(user);
        userDao.switchDataSource("db2");
        System.out.println(userDao.getCurrentDataSourceName());
        user = user.getDTO(1);
        user.setId(null);
        userDao.save(user);
        userDao.switchDataSource("db1");
//        throw new SimplifiedException("aa");
    }
}
