package com.shipping.service;

import org.spin.web.annotation.RestfulMethod;
import org.spin.web.annotation.RestfulService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>Created by xuweinan on 2017/9/17.</p>
 *
 * @author xuweinan
 */
@RestfulService("Test")
public class TestService {

    @RestfulMethod(auth = false, authRouter = "a")
    public int aaa(String[] pp, List a, MultipartFile[] b) {
//        throw new SimplifiedException("aaa");
        return pp.length;
    }
}
