package org.spin.wx;

import org.spin.core.gson.reflect.TypeToken;

import java.util.Objects;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/12/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class WxUrl<P, R> {
    private final String urlTmpl;
    private final TypeToken<R> resultType;

    public WxUrl(String urlTmpl, TypeToken<R> resultType) {
        this.urlTmpl = urlTmpl;
        this.resultType = resultType;
    }

    public String format(Object... args) {
        return String.format(urlTmpl, args);
    }

    public TypeToken<R> getResultType() {
        return resultType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WxUrl wxUrl = (WxUrl) o;
        return Objects.equals(urlTmpl, wxUrl.urlTmpl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlTmpl);
    }

    @Override
    public String toString() {
        return "WxUrl{" +
            "urlTmpl='" + urlTmpl + '\'' +
            '}';
    }
}
