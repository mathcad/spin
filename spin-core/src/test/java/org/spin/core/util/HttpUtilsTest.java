package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.util.MapUtils;
import org.spin.core.util.HttpUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilsTest {
    @Test
    public void httpGetRequest() throws Exception {
        String url = "http://4bvvnz4n.standard.51tppx.com/voter/29f617bf-ba9d-4e6d-a191-1787dba8047d?id=c0e7ac6f-4d12-4d87-8e49-7ce203223871&pc=1&from=timeline";
        String res = HttpUtils.get(url, MapUtils.ofMap("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.2.1; zh-cn; GT-S5660 Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1 MicroMessenger/4.5.255"));
        System.out.println(res);
        assertTrue(true);
    }
}
