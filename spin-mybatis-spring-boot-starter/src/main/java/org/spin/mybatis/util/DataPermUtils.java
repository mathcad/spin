package org.spin.mybatis.util;

import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.DataPermInfo;
import org.spin.mybatis.entity.AbstractDataPermEntity;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/8</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DataPermUtils {

    public void validatePerm(AbstractDataPermEntity entity) {
        DataPermInfo permInfo = CurrentUser.getCurrentNonNull().getDataPermInfo();

    }
}
