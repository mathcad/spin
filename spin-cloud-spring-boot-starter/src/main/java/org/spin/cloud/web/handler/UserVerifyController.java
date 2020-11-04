package org.spin.cloud.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.throwable.BizException;
import org.spin.web.AuthLevel;
import org.spin.web.ScopeType;
import org.spin.web.annotation.GetApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户附加验证Controller
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/8/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@RestController
@RequestMapping("/v1/user/biz")
public class UserVerifyController {
    private static final Logger logger = LoggerFactory.getLogger(UserVerifyController.class);

    private final UserVerifier verifier;

    public UserVerifyController(@Autowired(required = false) UserVerifier verifier) {
        this.verifier = verifier;
    }

    @GetApi(value = "verify", auth = AuthLevel.NONE, scope = ScopeType.INTERNAL, authors = "徐伟男")
    public UserVerifier.UserVerifyInfo requestMappingInfos(String uid, String t, Integer ct, Boolean vu, String ut) {
        UserVerifier.UserVerifyInfo verifyInfo;
        if (null != verifier) {
            verifyInfo = verifier.verify(uid, t, ct, Boolean.TRUE.equals(vu), ut);
            if (null == verifyInfo) {
                logger.error("业务线用户验证逻辑不能返回空");
                throw new BizException("业务线用户验证逻辑不正确, 请联系系统管理员");
            }
        } else {
            verifyInfo = new UserVerifier.UserVerifyInfo();
            verifyInfo.setName(uid);
        }
        return verifyInfo;
    }
}
