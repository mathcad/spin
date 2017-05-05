package org.spin.data.extend.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.LocalDate;

/**
 * 数据库Date与Java8的LocalDate之间的转换器
 * <p>Created by xuweinan on 2017/1/25.</p>
 *
 * @author xuweinan
 */
@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {

    @Override
    public Date convertToDatabaseColumn(LocalDate attribute) {
        return null == attribute ? null : Date.valueOf(attribute);
    }

    @Override
    public LocalDate convertToEntityAttribute(Date dbData) {
        return null == dbData ? null : dbData.toLocalDate();
    }
}
