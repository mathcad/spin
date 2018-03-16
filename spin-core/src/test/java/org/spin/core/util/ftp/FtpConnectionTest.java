package org.spin.core.util.ftp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Arvin on 2017/1/24.
 */
public class FtpConnectionTest {

//    @Test
    public void testFtp() {
        FtpConnection ftpConnection = new FtpConnection(Protocal.FTP, "arvin", "123", "localhost");
        boolean r = ftpConnection.retrieveFile(".", "oracle.txt", "D:\\");
        assertTrue(r);
    }

    @Test
    public void testIsActive() {
        FtpConnection ftpConnection = new FtpConnection(Protocal.FTP, "gsh56test", "123456", "192.168.20.234");
        ftpConnection.sendCommand("NOOP");
    }
}
