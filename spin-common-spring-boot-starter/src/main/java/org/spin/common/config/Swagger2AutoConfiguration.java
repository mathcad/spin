package org.spin.common.config;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.spin.common.config.properties.Swagger2Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
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
@ConditionalOnProperty(prefix = "swagger2", value = {"enable"}, havingValue = "true")
@EnableConfigurationProperties(Swagger2Properties.class)
public class Swagger2AutoConfiguration {

    @Bean
    public Docket restApiDocket(Swagger2Properties swagger2Properties) {

        List<Parameter> parameters = Collections.singletonList(
            new ParameterBuilder().name(HttpHeaders.AUTHORIZATION)
            .description("Access Token")
            .modelRef(new ModelRef("String"))
            .defaultValue("")
            .parameterType("header")
            .required(false)
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
            .globalOperationParameters(parameters);
    }

    private ApiInfo apiInfo(Swagger2Properties swagger2Properties) {
        return new ApiInfoBuilder()
            .title(swagger2Properties.getTitle())
            .contact(new Contact(swagger2Properties.getContactName(), swagger2Properties.getContactUrl(), swagger2Properties.getContactUrl()))
            .version(swagger2Properties.getVersion())
            .termsOfServiceUrl(swagger2Properties.getServiceUrl())
            .description(swagger2Properties.getDescription())
            .build();
    }
}
