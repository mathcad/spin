package org.spin.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * ui布局指令:UiComposition；定义一个页面组件，uiOverride会覆盖布局中定义一个区域；
 * UiComposition中的内容不会渲染，除了ui布局指令外 用法：
 * <@uiComposition src="layout.ftl">
 * <@uiOverride name="body"> Override body Content </@uiOverride>
 * </@uiComposition> 参数：src：布局文件
 * 
 */
public class UiComposition implements TemplateDirectiveModel {

	public final static String DIRECTIVE_NAME = "ui_composition";

	@Override
	public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {

		Writer freekMarkerWriter = env.getOut();
		env.setOut(new EmptyWriter(env.getOut()));
		if (body != null) {
			body.render(env.getOut());
		}
		env.setOut(freekMarkerWriter);

		String name = DirectiveUtils.getRequiredParam(params, "src");
		String encoding = DirectiveUtils.getParam(params, "encoding", null);
		String includeTemplateName = env.toFullTemplateName(getTemplatePath(env), name);
		env.include(includeTemplateName, encoding, true);

	}

	private class EmptyWriter extends Writer {

		private Writer out;

		public EmptyWriter(Writer out) {
			this.out = out;
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
		}

		@Override
		public void flush() throws IOException {
			out.flush();
		}

		@Override
		public void close() throws IOException {
			out.close();
		}
	}

	private String getTemplatePath(Environment env) {
		String templateName = env.getMainTemplate().getName();
		return templateName.lastIndexOf('/') == -1 ? "" : templateName.substring(0, templateName.lastIndexOf('/') + 1);
	}
}