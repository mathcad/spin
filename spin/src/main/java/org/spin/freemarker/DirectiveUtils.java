package org.spin.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.spin.core.util.StringUtils;

import java.util.Map;

public final class DirectiveUtils {
    public static final String UI_OVERRIDE = "__ftl_override__";
    public static final String UI_DEFINE = "__ftl_define__";
    public static final String OVERRIDE_CURRENT_NODE = "__ftl_override_current_node";

    private DirectiveUtils() {
    }

    public static String getOverrideVariableName(String name) {
        return UI_OVERRIDE + name;
    }

    public static String getDefineVariableName(String name) {
        return UI_DEFINE + name;
    }

    public static String getRequiredParam(Map params, String key) throws TemplateException {
        Object value = params.get(key);
        if (value == null || StringUtils.isEmpty(value.toString())) {
            throw new TemplateModelException("not found required parameter:" + key + " for directive");
        }
        return value.toString();
    }

    public static String getParam(Map params, String key, String defaultValue) throws TemplateException {
        Object value = params.get(key);
        return value == null ? defaultValue : value.toString();
    }

    public static TemplateDirectiveBodyOverrideWraper getOverrideBody(Environment env, String name) throws TemplateModelException {
        return (TemplateDirectiveBodyOverrideWraper) env.getVariable(DirectiveUtils.getOverrideVariableName(name));
    }

    public static TemplateDirectiveBodyOverrideWraper getDefineBody(Environment env, String name) throws TemplateModelException {
        return (TemplateDirectiveBodyOverrideWraper) env.getVariable(DirectiveUtils.getDefineVariableName(name));
    }

    public static void setTopBodyForParentBody(TemplateDirectiveBodyOverrideWraper topBody, TemplateDirectiveBodyOverrideWraper overrideBody) {
        TemplateDirectiveBodyOverrideWraper parent = overrideBody;
        while (parent.parentBody != null) {
            parent = parent.parentBody;
        }
        parent.parentBody = topBody;
    }
}
