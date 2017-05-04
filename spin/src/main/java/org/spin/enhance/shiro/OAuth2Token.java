package org.spin.enhance.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * OAuth2的token
 *
 * @author xuweinan
 */
public class OAuth2Token implements AuthenticationToken {
    private String credentials;
    private String principal;

    public OAuth2Token(String principal, String credentials) {
        this.principal = principal;
        this.credentials = credentials;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
