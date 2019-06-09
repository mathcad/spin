package org.spin.common.config;

import org.spin.core.util.DateUtils;
import org.spin.core.util.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * description 日期参数接收转换器，将json字符串转为日期类型
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/13.</p>
 */
@Configuration
public class ConverterAutoConfiguration {
    
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConvert() {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                return DateUtils.toLocalDateTime(source);
            }
        };
    }

    @Bean
    public Converter<String, LocalDate> localDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String s) {
                if (StringUtils.isNotBlank(s))
                    return DateUtils.toLocalDateTime(s).toLocalDate();
                else
                    return null;
            }
        };
    }

    @Bean
    public Converter<String, LocalTime> localTimeConverter() {
        return new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String s) {
                if (StringUtils.isNotBlank(s))
                    return LocalTime.parse(s, DateTimeFormatter.ISO_LOCAL_TIME);
                else
                    return null;
            }
        };
    }

    @Bean
    public Converter<String, Date> dateConverter() {
        return new Converter<String, Date>() {
            @Override
            public Date convert(String s) {
                return DateUtils.toDate(s);
            }
        };
    }
}
