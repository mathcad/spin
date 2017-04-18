package org.spin.freemarker;

import java.io.IOException;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * ui布局指令:UiDefine；在布局中定义一个区域
 * 用法：
 * 参数：name 区域的名称
 */
public class UiDefine implements TemplateDirectiveModel {
    public final static String DIRECTIVE_NAME = "ui_define";

    public void execute(Environment env,
                        @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {

        String name = DirectiveUtils.getRequiredParam(params, "name");

        TemplateDirectiveBodyOverrideWraper current = new TemplateDirectiveBodyOverrideWraper(body, env);
        TemplateDirectiveBodyOverrideWraper override = DirectiveUtils.getOverrideBody(env, name);

        if (override != null) {
            override.render(env.getOut());
        } else if (body != null) {
            current.render(env.getOut());
        }
    }
}
