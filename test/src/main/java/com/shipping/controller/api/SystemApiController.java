package com.shipping.controller.api;

import com.shipping.domain.dto.LoginInfo;
import com.shipping.internal.InfoCache;
import com.shipping.service.SystemService;
import com.shipping.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.auth.SecretManager;
import org.spin.core.security.RSA;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;
import org.spin.web.RestfulResponse;
import org.spin.web.annotation.Needed;
import org.spin.web.annotation.RestfulApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

/**
 * 系统功能API
 * <p>
 * Created by xuweinan on 2016/10/3.
 *
 * @author xuweinan
 */
@RestController
@RequestMapping("/api")
public class SystemApiController {

    private static final Logger logger = LoggerFactory.getLogger(SystemApiController.class);

    @Autowired
    private SecretManager secretManager;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemService systemService;

    /**
     * 登录
     */
    @RestfulApi(auth = false, name = "apiLogin", path = "login", method = RequestMethod.POST)
    public RestfulResponse login(String identity, String password, String key, String code) throws InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        logger.info("InMethod: login({}, {}, {}, {})", identity, password, key, code);

        // 如果有密钥，使用密钥验证登录
        if (StringUtils.isNotEmpty(key))
            return RestfulResponse.ok(userService.keyLogin(key));
        // 如果有code，微信登录
        if (StringUtils.isNotEmpty(code))
            return RestfulResponse.ok(userService.wxLogin(code));
        // 没有密钥，常规验证
        if (StringUtils.isEmpty(identity) || StringUtils.isEmpty(password))
            throw new SimplifiedException("请求参数不完整");
        String i = RSA.decrypt(InfoCache.RSA_PRIKEY, identity);
        String p = RSA.decrypt(InfoCache.RSA_PRIKEY, password);
        return RestfulResponse.ok(userService.login(i, p));

    }

    @RestfulApi(auth = false, name = "apiLogout", path = "logout", method = RequestMethod.POST)
    public RestfulResponse logout(@Needed String key) {
        userService.logout(key);
        return RestfulResponse.ok();
    }

    /**
     * 获取token
     *
     * @param key 密钥
     */
    @RestfulApi(auth = false, name = "getToken", path = "getToken", method = RequestMethod.POST)
    public RestfulResponse getToken(@Needed String key) {
        LoginInfo dto = new LoginInfo();
        dto.setTokenInfo(secretManager.generateToken(key), secretManager.getTokenExpiredIn());
        return RestfulResponse.ok(dto.getTokenInfo());
    }

    /**
     * 获取菜单树
     */
    @RestfulApi(path = "menuTree", method = RequestMethod.GET)
    public RestfulResponse getMenu() {
        return RestfulResponse.ok();
    }

    @RestfulApi(path = "getRegions", method = RequestMethod.GET)
    public RestfulResponse getRegions() {
        return RestfulResponse.ok(systemService.getRegions());
    }
}
