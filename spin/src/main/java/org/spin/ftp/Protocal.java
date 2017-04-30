package org.spin.ftp;

/**
 * FTP协议
 */
public enum Protocal {
    FTP("ftp"), FTPS("ftps");
    private String value;

    Protocal(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
