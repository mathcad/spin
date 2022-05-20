package org.spin.cloud.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.spin.cloud.config.properties.Swagger2Properties;
import org.spin.cloud.swagger.OperationAuthBuilderPlugin;
import org.spin.core.util.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description swagger2 Configuration
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/13.</p>
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
@EnableConfigurationProperties(Swagger2Properties.class)
public class Swagger2AutoConfiguration {

    @Value("${app.version}")
    private String version;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    @ConditionalOnProperty(prefix = "swagger2", value = {"enable"}, havingValue = "true")
    public OperationAuthBuilderPlugin operationAuthBuilderPlugin() {
        return new OperationAuthBuilderPlugin();
    }

    @Bean
    public BeanPostProcessor swaggerBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }
        };
    }

    private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
        List<T> copy = mappings.stream()
            .filter(m -> m.getPatternParser() == null)
            .collect(Collectors.toList());
        mappings.clear();
        mappings.addAll(copy);
    }

    private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
        return BeanUtils.getFieldValue(bean, "handlerMappings");
    }

    @Bean
    @ConditionalOnProperty(prefix = "swagger2", value = {"enable"}, havingValue = "true")
    public Docket restApiDocket(Swagger2Properties swagger2Properties) {
        List<RequestParameter> parameters = Collections.singletonList(
            new RequestParameterBuilder()
                .name("Client-Info")
                .description("客户端信息")
                .required(false)
                .in(ParameterType.HEADER)
                .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)))
                .example(new ExampleBuilder().value("").build())
                .parameterIndex(Integer.MIN_VALUE)
                .build()
        );

        return new Docket(swagger2Properties.getDocType().documentType())
            .apiInfo(apiInfo(swagger2Properties))
            .select()
            .apis(RequestHandlerSelectors.basePackage(swagger2Properties.getBasePackage()))
            .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
            .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
            .paths(PathSelectors.any())
            .build()
            .globalRequestParameters(parameters);
    }

    private ApiInfo apiInfo(Swagger2Properties swagger2Properties) {
        return new ApiInfoBuilder()
            .title(swagger2Properties.getTitle())
            .contact(new Contact(swagger2Properties.getContactName(), swagger2Properties.getContactUrl(), swagger2Properties.getContactUrl()))
            .version(version)
            .termsOfServiceUrl(swagger2Properties.getServiceUrl())
            .description(swagger2Properties.getDescription())
            .build();
    }
}
