package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * <p>Created by xuweinan on 2018/5/11.</p>
 *
 * @author xuweinan
 */
class XmlUtilsTest {

    @Test
    void travelDocument() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream fis = loader.getResourceAsStream("user.xml")) {
            XmlUtils utils = new XmlUtils(fis);
            Map<String, String> stringStringMap = utils.travelDocument();
            System.out.println(JsonUtils.toJson(stringStringMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
