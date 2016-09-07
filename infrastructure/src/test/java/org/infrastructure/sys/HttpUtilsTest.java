package org.infrastructure.sys;

import org.infrastructure.util.HttpUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpUtilsTest {
    @Test
    public void httpGetRequest() throws Exception {
        String res = HttpUtils.httpGetRequest("http://api.map.baidu.com/direction/v1/routematrix?output=json&origins={}&destinations={}&ak={}","芜湖","广州","vSNWDgUvUAMI0yLz2N23NTNB");
        System.out.println(res);
        assertTrue(true);
    }
}