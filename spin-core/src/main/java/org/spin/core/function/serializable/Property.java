package org.spin.core.function.serializable;

import java.io.Serializable;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/3/29</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@FunctionalInterface
public interface Property<T, R> extends Function  <T, R>, Serializable {

}
