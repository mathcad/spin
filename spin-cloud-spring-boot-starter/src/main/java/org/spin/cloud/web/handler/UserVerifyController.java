package org.spin.cloud.web.handler;

import org.spin.core.util.StringUtils;
import org.spin.web.AuthLevel;
import org.spin.web.ScopeType;
import org.spin.web.annotation.GetApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    private final UserVerifier verifier;

    public UserVerifyController(@Autowired(required = false) UserVerifier verifier) {
        this.verifier = verifier;
    }

    @GetApi(value = "verify", auth = AuthLevel.NONE, scope = ScopeType.INTERNAL, authors = "徐伟男")
    public String requestMappingInfos(String uid, String t, Integer ct, Boolean vu, String ut) {
        if (null != verifier) {
            List<Long> ids = verifier.verify(uid, t, ct, Boolean.TRUE.equals(vu), ut);
            return StringUtils.join(ids, ",");
        }

        return null;
    }
}
