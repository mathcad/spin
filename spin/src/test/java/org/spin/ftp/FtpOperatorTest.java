package org.spin.ftp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Arvin on 2017/1/24.
 */
public class FtpOperatorTest {

    @Test
    public void testFtp() {
        FtpOperator ftpOperator = FtpOperator.connect(FtpOperator.Protocal.FTP, "arvin", "123", "localhost");
        ftpOperator.downFile(".", "oracle.txt", "D:\\");
        assertTrue(true);
    }
}