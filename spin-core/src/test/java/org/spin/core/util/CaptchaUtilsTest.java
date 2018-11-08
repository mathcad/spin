package org.spin.core.util;

import java.io.File;
import java.io.IOException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/11/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class CaptchaUtilsTest {

    public static void main(String[] args) throws IOException {
        File dir = new File("D:/verifies");
        int w = 150, h = 50;
        for (int i = 0; i < 50; i++) {
            String verifyCode = CaptchaUtils.generateVerifyCode(4);
            File file = new File(dir, verifyCode + ".jpg");
            CaptchaUtils.outputImage(w, h, file, verifyCode);
        }
    }
}
