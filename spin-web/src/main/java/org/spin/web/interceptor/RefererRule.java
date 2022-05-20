package org.spin.web.interceptor;

import java.util.regex.Pattern;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/12/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RefererRule {
    private final Pattern urlPattern;
    private final Pattern refererPattern;

    public RefererRule(String urlReg, String refererReg) {
        urlPattern = Pattern.compile(urlReg);
        refererPattern = Pattern.compile(refererReg);
    }

    public Pattern getUrlPattern() {
        return urlPattern;
    }


    public Pattern getRefererPattern() {
        return refererPattern;
    }

}
