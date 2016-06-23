package org.infrastructure.freemarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import freemarker.cache.TemplateLoader;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.template.Configuration;

public class FreeMarkerConfigurerWrapper {
	
	private Resource configLocation;

	private Properties freemarkerSettings;

	private Map<String, Object> freemarkerVariables;

	private String defaultEncoding;

	private final List<TemplateLoader> templateLoaders = new ArrayList<TemplateLoader>();

	private List<TemplateLoader> preTemplateLoaders;

	private List<TemplateLoader> postTemplateLoaders;

	private String[] templateLoaderPaths;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private boolean preferFileSystemAccess = true;
	
	
	private Configuration configuration;

	private TaglibFactory taglibFactory;

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public TaglibFactory getTaglibFactory() {
		return taglibFactory;
	}

	public Resource getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	public Properties getFreemarkerSettings() {
		return freemarkerSettings;
	}

	public void setFreemarkerSettings(Properties freemarkerSettings) {
		this.freemarkerSettings = freemarkerSettings;
	}

	public Map<String, Object> getFreemarkerVariables() {
		return freemarkerVariables;
	}

	public void setFreemarkerVariables(Map<String, Object> freemarkerVariables) {
		this.freemarkerVariables = freemarkerVariables;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public List<TemplateLoader> getPreTemplateLoaders() {
		return preTemplateLoaders;
	}

	public void setPreTemplateLoaders(List<TemplateLoader> preTemplateLoaders) {
		this.preTemplateLoaders = preTemplateLoaders;
	}

	public List<TemplateLoader> getPostTemplateLoaders() {
		return postTemplateLoaders;
	}

	public void setPostTemplateLoaders(List<TemplateLoader> postTemplateLoaders) {
		this.postTemplateLoaders = postTemplateLoaders;
	}

	public String[] getTemplateLoaderPaths() {
		return templateLoaderPaths;
	}

	public void setTemplateLoaderPath(String templateLoaderPath) {
		this.templateLoaderPaths = new String[] {templateLoaderPath};
	}
	
	public void setTemplateLoaderPaths(String... templateLoaderPaths) {
		this.templateLoaderPaths = templateLoaderPaths;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public boolean isPreferFileSystemAccess() {
		return preferFileSystemAccess;
	}

	public void setPreferFileSystemAccess(boolean preferFileSystemAccess) {
		this.preferFileSystemAccess = preferFileSystemAccess;
	}

	public List<TemplateLoader> getTemplateLoaders() {
		return templateLoaders;
	}

	public void setTaglibFactory(TaglibFactory taglibFactory) {
		this.taglibFactory = taglibFactory;
	}
}
