package org.spin.core.util.ftp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Arvin on 2017/1/24.
 */
public class FtpOperatorTest {

    @Test
    public void testFtp() {
        FtpOperator ftpOperator = FtpOperator.connect(Protocal.FTP, "arvin", "123", "localhost");
        ftpOperator.downFile(".", "oracle.txt", "D:\\");
        assertTrue(true);
    }
}
