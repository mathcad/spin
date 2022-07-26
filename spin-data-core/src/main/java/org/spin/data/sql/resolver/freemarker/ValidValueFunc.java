package org.spin.data.sql.resolver.freemarker;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ObjectUtils;
import org.spin.core.util.StringUtils;

import java.util.List;

/**
 * 用来判断标签参数值，是否有效
 *
 * @author xuweinan
 * @version V1.0
 */
public class ValidValueFunc implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        boolean valid;
        Object o;
        if (arguments.get(0) == null) {
            o = null;
        } else if (arguments.get(0) instanceof TemplateBooleanModel) {
            o = ((TemplateBooleanModel) arguments.get(0)).getAsBoolean();
        } else if (arguments.get(0) instanceof SimpleScalar) {
            o = ((SimpleScalar) arguments.get(0)).getAsString();
        } else
            o = arguments.get(0).toString();

        if (arguments.size() == 1) {
            if (o == null)
                valid = false;
            else if (o instanceof Boolean) {
                valid = (Boolean) o;
            } else {
                valid = !(o instanceof String) || StringUtils.isNotEmpty((String) o);
            }
        } else if (arguments.size() == 2) {
            String o1 = arguments.get(1) == null ? null : arguments.get(1).toString();
            if (o == null)
                valid = o1 == null;
            else {
                valid = ObjectUtils.nullSafeEquals(o1, o);
            }
        } else
            throw new SimplifiedException("V函数参数个数不正确（需1,2个）");

        return valid;
    }
}
