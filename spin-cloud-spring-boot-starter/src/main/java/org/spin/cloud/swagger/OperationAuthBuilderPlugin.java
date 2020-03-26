package org.spin.cloud.swagger;

import com.github.xiaoymin.knife4j.spring.plugin.AbstractOperationBuilderPlugin;
import com.google.common.collect.Lists;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.AuthLevel;
import org.spin.web.annotation.Auth;
import org.spin.web.annotation.Author;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.LinkedList;
import java.util.List;

/**
 * Swagger 认证相关属性插件
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/6</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class OperationAuthBuilderPlugin extends AbstractOperationBuilderPlugin {

    /***
     * 添加作者属性
     * @param context 接口上下文
     */
    @Override
    public void apply(OperationContext context) {
        List<Parameter> additionalParameters = new LinkedList<>();

        HandlerMethod handlerMethod = BeanUtils.getFieldValue(context, "requestContext.handler.handlerMethod");
        Author authorAnno = handlerMethod.getMethodAnnotation(Author.class);
        if (null != authorAnno) {
            StringBuilder sb = new StringBuilder();
            if (!CollectionUtils.isEmpty(authorAnno.value())) {
                sb.append(StringUtils.join(authorAnno.value(), ","));
            } else {
                sb.append("未知");
            }

            if (StringUtils.isNotEmpty(authorAnno.department())) {
                sb.append(":").append(authorAnno.department());
            }

            if (StringUtils.isNotEmpty(authorAnno.contact())) {
                sb.append("  (").append(authorAnno.contact()).append(")");
            }
            context.operationBuilder().extensions(Lists.newArrayList(new StringVendorExtension("x-author", sb.toString())));
        }


        Auth authAnno = handlerMethod.getMethodAnnotation(Auth.class);
        if (null != authAnno) {
            context.operationBuilder().extensions(Lists.newArrayList(new StringVendorExtension("x-auth", authAnno.value().getDesc())));
            context.operationBuilder().extensions(Lists.newArrayList(new StringVendorExtension("x-scope", authAnno.scope().name())));
            context.operationBuilder().extensions(Lists.newArrayList(new StringVendorExtension("x-authName", authAnno.name())));

            if (!CollectionUtils.isEmpty(authAnno.roles())) {
                context.operationBuilder().extensions(Lists.newArrayList(new StringVendorExtension("x-roles", StringUtils.join(authAnno.roles(), ","))));
            }

            if (AuthLevel.NONE != authAnno.value()) {
                additionalParameters.add(new ParameterBuilder()
                    .parameterType("header")
                    .description("访问凭证")
                    .modelRef(new ModelRef("String"))
                    .required(true)
                    .allowEmptyValue(false)
                    .order(Ordered.HIGHEST_PRECEDENCE + 1)
                    .name(HttpHeaders.AUTHORIZATION).build());
            }
        }

        if (!additionalParameters.isEmpty()) {
            context.operationBuilder().parameters(additionalParameters);
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
