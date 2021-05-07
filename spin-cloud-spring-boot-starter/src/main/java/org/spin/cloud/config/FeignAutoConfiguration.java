package org.spin.cloud.config;

import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.MultipartFormContentProcessor;
import feign.form.spring.SpringFormEncoder;
import feign.optionals.OptionalDecoder;
import org.spin.cloud.feign.FeignInterceptor;
import org.spin.cloud.feign.RestfulHandledDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.support.AbstractFormWriter;
import org.springframework.cloud.openfeign.support.PageableSpringEncoder;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static feign.form.ContentType.MULTIPART;

/**
 * Feign客户端自动配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(FeignClientsConfiguration.class)
public class FeignAutoConfiguration {

    static {
        new SpinCloudAsyncInterceptor().register();
    }

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Autowired(required = false)
    private SpringDataWebProperties springDataWebProperties;

    @Bean
    public Decoder feignDecoder() {
        return new OptionalDecoder(new ResponseEntityDecoder(new RestfulHandledDecoder(this.messageConverters)));
    }

    @Bean
    @ConditionalOnMissingClass("org.springframework.data.domain.Pageable")
    public Encoder feignEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider) {
        return springEncoder(formWriterProvider);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
    public Encoder feignEncoderPageable(ObjectProvider<AbstractFormWriter> formWriterProvider) {
        PageableSpringEncoder encoder = new PageableSpringEncoder(springEncoder(formWriterProvider));

        if (springDataWebProperties != null) {
            encoder.setPageParameter(springDataWebProperties.getPageable().getPageParameter());
            encoder.setSizeParameter(springDataWebProperties.getPageable().getSizeParameter());
            encoder.setSortParameter(springDataWebProperties.getSort().getSortParameter());
        }
        return encoder;
    }

    @Bean
    @ConditionalOnMissingBean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }


//    @Configuration
//    @ConditionalOnClass(name = "feign.hystrix.HystrixFeign")
//    protected static class HystrixFeignTargeterConfiguration {
//
//        @Bean("spinFeignTargeter")
//        @ConditionalOnMissingBean
//        public Targeter feignTargeter() {
//            return new HystrixTargeter();
//        }
//
//    }

//    @Configuration
//    @ConditionalOnMissingClass("feign.hystrix.HystrixFeign")
//    protected static class DefaultFeignTargeterConfiguration {
//
//        @Bean("spinFeignTargeter")
//        @ConditionalOnMissingBean
//        public Targeter feignTargeter() {
//            return new DefaultTargeter();
//        }
//
//    }

    @Bean
    public RequestInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }

//    @Bean
//    @ConditionalOnMissingClass("org.springframework.data.domain.Pageable")
//    public Encoder feignEncoder() {
//        return new AuthSpringEncoder(messageConverters);
//    }
//
//    @Bean
//    @ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
//    public Encoder feignEncoderPageable() {
//        return new PageableSpringEncoder(new AuthSpringEncoder(this.messageConverters));
//    }

    private Encoder springEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider) {
        AbstractFormWriter formWriter = formWriterProvider.getIfAvailable();

        if (formWriter != null) {
            return new SpringEncoder(new SpringPojoFormEncoder(formWriter), this.messageConverters);
        } else {
            return new SpringEncoder(new SpringFormEncoder(), this.messageConverters);
        }
    }

    private static class SpringPojoFormEncoder extends SpringFormEncoder {

        SpringPojoFormEncoder(AbstractFormWriter formWriter) {
            super();

            MultipartFormContentProcessor processor = (MultipartFormContentProcessor) getContentProcessor(MULTIPART);
            processor.addFirstWriter(formWriter);
        }

    }
}
