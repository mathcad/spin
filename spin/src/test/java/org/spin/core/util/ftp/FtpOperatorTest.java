package org.spin.core.util.ftp;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Arvin on 2017/1/24.
 */
public class FtpOperatorTest {

//    @Test
    public void testFtp() {
        FtpOperator ftpOperator = new FtpOperator(Protocal.FTP, "arvin", "123", "localhost");
        boolean r = ftpOperator.retrieveFile(".", "oracle.txt", "D:\\");
        assertTrue(r);
    }
}
