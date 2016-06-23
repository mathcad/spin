package org.infrastructure.freemarker;

import java.io.IOException;
import java.io.Writer;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class TemplateDirectiveBodyOverrideWraper implements TemplateDirectiveBody,TemplateModel{
	private TemplateDirectiveBody body;
	public TemplateDirectiveBodyOverrideWraper parentBody;
	public Environment env;
	
	public TemplateDirectiveBodyOverrideWraper(TemplateDirectiveBody body,
			Environment env) {
		super();
		this.body = body;
		this.env = env;
	}
	
	public void render(Writer out) throws TemplateException, IOException {
		if(body == null) return;
		TemplateDirectiveBodyOverrideWraper preOverridy = (TemplateDirectiveBodyOverrideWraper)env.getVariable(DirectiveUtils.OVERRIDE_CURRENT_NODE);
		try {
			env.setVariable(DirectiveUtils.OVERRIDE_CURRENT_NODE, this);
			body.render(out);
		}finally {
			env.setVariable(DirectiveUtils.OVERRIDE_CURRENT_NODE, preOverridy);
		}
	}
}
