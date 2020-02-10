package org.spin.core.util.http.useragent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class UserAgentUtilTest {

    @Test
    void parse() {
        UserAgent parse = UserAgentParser.parse("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36");
//        parse = UserAgentUtil.parse("PostmanRuntime/7.17.1");
        System.out.println(parse);
    }
}
