package org.spin.web.util;

import org.spin.core.util.DateUtils;

/**
 * WEB环境工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class WebUtils {

    private WebUtils() {
    }

    public static void main(String[] args) {
        System.out.println(DateUtils.toDate("1989/12/12 12:12:12"));

    }

}
