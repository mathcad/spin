package org.infrastructure.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.infrastructure.util.StringUtils;

import java.util.Map;

/**
 * @author badqiu
 */
public class DirectiveUtils {

	public static String UI_OVERRIDE = "__ftl_override__";
	public static String UI_DEFINE = "__ftl_define__";
	public static String OVERRIDE_CURRENT_NODE = "__ftl_override_current_node";

	public static String getOverrideVariableName(String name) {
		return UI_OVERRIDE + name;
	}

	public static String getDefineVariableName(String name) {
		return UI_DEFINE + name;
	}

//	public static void exposeRapidMacros(Configuration conf) {
//		conf.setSharedVariable(BlockDirective.DIRECTIVE_NAME, new BlockDirective());
//		conf.setSharedVariable(ExtendsDirective.DIRECTIVE_NAME, new ExtendsDirective());
//		conf.setSharedVariable(OverrideDirective.DIRECTIVE_NAME, new OverrideDirective());
//		conf.setSharedVariable(SuperDirective.DIRECTIVE_NAME, new SuperDirective());
//	}

	public static String getRequiredParam(@SuppressWarnings("rawtypes") Map params,String key) throws TemplateException {
		Object value = params.get(key);
		if(value == null || StringUtils.isEmpty(value.toString())) {
			throw new TemplateModelException("not found required parameter:"+key+" for directive");
		}
		return value.toString();
	}

	public static String getParam(@SuppressWarnings("rawtypes") Map params,String key,String defaultValue) throws TemplateException {
		Object value = params.get(key);
		return value == null ? defaultValue : value.toString();
	}

	public static TemplateDirectiveBodyOverrideWraper getOverrideBody(Environment env, String name) throws TemplateModelException {
		TemplateDirectiveBodyOverrideWraper value = (TemplateDirectiveBodyOverrideWraper)env.getVariable(DirectiveUtils.getOverrideVariableName(name));
		return value;
	}

	public static TemplateDirectiveBodyOverrideWraper getDefineBody(Environment env, String name) throws TemplateModelException {
		TemplateDirectiveBodyOverrideWraper value = (TemplateDirectiveBodyOverrideWraper)env.getVariable(DirectiveUtils.getDefineVariableName(name));
		return value;
	}

	public static void setTopBodyForParentBody(Environment env,
											   TemplateDirectiveBodyOverrideWraper topBody,
											   TemplateDirectiveBodyOverrideWraper overrideBody) {
		TemplateDirectiveBodyOverrideWraper parent = overrideBody;
		while(parent.parentBody != null) {
			parent = parent.parentBody;
		}
		parent.parentBody = topBody;
	}
}
