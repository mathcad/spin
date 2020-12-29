package org.spin.cloud.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.spin.cloud.config.properties.Swagger2Properties;
import org.spin.cloud.swagger.OperationAuthBuilderPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ExampleBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterStyle;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

/**
 * description swagger2 Configuration
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/13.</p>
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
@ConditionalOnProperty(prefix = "swagger2", value = {"enable"}, havingValue = "true")
@EnableConfigurationProperties(Swagger2Properties.class)
public class Swagger2AutoConfiguration {

    @Value("${app.version}")
    private String version;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public OperationAuthBuilderPlugin operationAuthBuilderPlugin() {
        return new OperationAuthBuilderPlugin();
    }

    @Bean
    public Docket restApiDocket(Swagger2Properties swagger2Properties) {
        List<RequestParameter> parameters = Collections.singletonList(
            new RequestParameterBuilder().name("Client-Info")
                .description("客户端信息")
                .example(new ExampleBuilder().value("").build())
                .in(ParameterType.HEADER)
                .required(false)
                .query(q -> q.style(ParameterStyle.SIMPLE).model(m -> m.scalarModel(ScalarType.STRING)))
                .parameterIndex(Ordered.HIGHEST_PRECEDENCE)
                .build()
        );

        return new Docket(DocumentationType.SWAGGER_2)
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
