package org.infrastructure.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.List;

/**
 * 与org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
 * 类似，但不需要web环境支持
 *
 * @author zx
 * @see org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
 */
public class SimpleFreeMarkerConfigurer extends FreeMarkerConfigurationFactory
        implements InitializingBean, ResourceLoaderAware {

    private Configuration configuration;


    /**
     * Set a preconfigured Configuration to use for the FreeMarker web config, e.g. a
     * shared one for web and email usage, set up via FreeMarkerConfigurationFactoryBean.
     * If this is not set, FreeMarkerConfigurationFactory's properties (inherited by
     * this class) have to be specified.
     *
     * @see org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Initialize FreeMarkerConfigurationFactory's Configuration
     * if not overridden by a preconfigured FreeMarker Configuation.
     * <p>Sets up a ClassTemplateLoader to use for loading Spring macros.
     *
     * @see #createConfiguration
     * @see #setConfiguration
     */
    public void afterPropertiesSet() throws IOException, TemplateException {
        if (this.configuration == null) {
            this.configuration = createConfiguration();
        }
    }

    /**
     * This implementation registers an additional ClassTemplateLoader
     * for the Spring-provided macros, added to the end of the list.
     */
    @Override
    protected void postProcessTemplateLoaders(List<TemplateLoader> templateLoaders) {
        templateLoaders.add(new ClassTemplateLoader(FreeMarkerConfigurer.class, ""));
        logger.info("ClassTemplateLoader for Spring macros added to FreeMarker configuration");
    }


    /**
     * Return the Configuration object wrapped by this bean.
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }
}
