package org.spin.sys.auth;

/**
 * Created by Arvin on 2017/3/20.
 */
public class SecretInfo {
    private String identifier;
    private String key;
    private Long keyGenTiem;
    private String token;
    private Long tokenGenTime;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getKeyGenTiem() {
        return keyGenTiem;
    }

    public void setKeyGenTiem(Long keyGenTiem) {
        this.keyGenTiem = keyGenTiem;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getTokenGenTime() {
        return tokenGenTime;
    }

    public void setTokenGenTime(Long tokenGenTime) {
        this.tokenGenTime = tokenGenTime;
    }
}
