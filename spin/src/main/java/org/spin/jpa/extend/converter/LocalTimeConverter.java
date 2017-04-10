package org.spin.jpa.extend.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Time;
import java.time.LocalTime;

/**
 * 数据库Time与Java8的LocalTime之间的转换器
 * Created by xuweinan on 2017/1/25.
 *
 * @author xuweinan
 */
@Converter(autoApply = true)
public class LocalTimeConverter implements AttributeConverter<LocalTime, Time> {

    @Override
    public Time convertToDatabaseColumn(LocalTime attribute) {
        return null == attribute ? null : Time.valueOf(attribute);
    }

    @Override
    public LocalTime convertToEntityAttribute(Time dbData) {
        return null == dbData ? null : dbData.toLocalTime();
    }
}
