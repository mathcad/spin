package org.spin.core.function;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/2/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@FunctionalInterface
public interface ExceptionalHandler {
    void handle() throws Exception;
}
