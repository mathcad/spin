package org.spin.jpa.extend.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 数据库Timestamp与Java8的LocalDateTime之间的转换器
 * Created by xuweinan on 2017/1/25.
 *
 * @author xuweinan
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime attribute) {
        return null == attribute ? null : Timestamp.valueOf(attribute);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp dbData) {
        return null == dbData ? null : dbData.toLocalDateTime();
    }
}