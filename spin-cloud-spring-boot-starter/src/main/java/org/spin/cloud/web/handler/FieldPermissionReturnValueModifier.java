package org.spin.cloud.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.util.Env;
import org.spin.cloud.util.PermissionCache;
import org.spin.core.Assert;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.handler.RequestResponseBodyModifier;

import java.util.Map;

/**
 * 字段权限返回结果处理器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class FieldPermissionReturnValueModifier implements RequestResponseBodyModifier {
    private static final Logger logger = LoggerFactory.getLogger(FieldPermissionReturnValueModifier.class);

    @Override
    public Object modify(Object returnValue) {
        if (null == returnValue) {
            return null;
        }

        String apiCode = Env.getCurrentApiCode();
        if (StringUtils.isNotEmpty(apiCode)) {
            logger.info("处理字段权限:" + apiCode);
            Map<String, String> fieldsToClean = PermissionCache.getFieldsToClean("FIELD" + apiCode.substring(3));
            if (fieldsToClean.size() > 0) {
                fieldsToClean.forEach((k, v) -> {
                    try {
                        setFieldValue(returnValue, k);
                    } catch (Exception e) {
                        logger.error("处理字段权限出现异常: " + k + "-" + v, e);
                    }
                });
            }
        }
        return returnValue;
    }

    private void setFieldValue(Object target, String fieldPath) {
        int idx = fieldPath.indexOf('*');
        if (idx < 0) {
            BeanUtils.setFieldValue(target, fieldPath, null);
        } else {
            Assert.isTrue(fieldPath.charAt(idx - 1) == '[' && fieldPath.charAt(idx + 1) == ']', "索引下标通配符只能在\"[]\"中使用: " + fieldPath);
            String left = fieldPath.substring(0, idx - 1);
            String right = fieldPath.substring(idx + 2);

            Object fieldValue = BeanUtils.getFieldValue(target, left);
            Assert.isTrue(null == fieldValue || CollectionUtils.isCollection(fieldValue),
                () -> "索引下标通配符只能用于集合类型: " + left + "实际类型为" + fieldValue.getClass().getName());
            if (null == fieldValue) {
                return;
            }
            if (fieldValue instanceof Iterable) {
                ((Iterable<?>) fieldValue).forEach(it -> {
                    setFieldValue(it, right);
                });
            } else if (fieldValue.getClass().isArray()) {
                Object[] values = (Object[]) fieldValue;
                for (Object it : values) {
                    setFieldValue(it, right);
                }
            }
        }
    }
}
