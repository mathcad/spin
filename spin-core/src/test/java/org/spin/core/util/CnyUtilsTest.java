package org.spin.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/8/21</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class CnyUtilsTest {

    @Test
    void convertCNY() {
        System.out.println(CnyUtils.convertToCNY("-1013.256"));
        System.out.println(CnyUtils.convertToCNY("0.25"));
    }

    @Test
    void convertCNY2() {
        System.out.println(CnyUtils.convertFromCNY("负壹仟零壹拾叁元贰角陆分"));
        System.out.println(CnyUtils.convertFromCNY("负贰角陆分"));
        System.out.println(CnyUtils.convertFromCNY("贰角陆分"));
        System.out.println(CnyUtils.convertFromCNY("负壹仟零壹拾叁元整"));
        System.out.println(CnyUtils.convertFromCNY("壹仟零壹拾叁元整"));
    }
}
