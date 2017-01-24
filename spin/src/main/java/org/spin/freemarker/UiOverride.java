package org.spin.freemarker;

import java.io.IOException;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * ui布局指令:UiOverride；覆盖布局中定义一个区域
 * 用法：
 * <@uiOverride name="body">
 * 参数：name 区域的名称
 *
 * @author zx
 */
public class UiOverride implements TemplateDirectiveModel {

    public final static String DIRECTIVE_NAME = "ui_override";

    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        String name = DirectiveUtils.getRequiredParam(params, "name");
        String overrideVariableName = DirectiveUtils.getOverrideVariableName(name);

        TemplateDirectiveBodyOverrideWraper current = new TemplateDirectiveBodyOverrideWraper(body, env);

        if (body != null) {
            env.setVariable(overrideVariableName, current);
        }
    }

}
