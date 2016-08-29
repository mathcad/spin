package org.zibra.io.serialize.java8;

import org.zibra.io.serialize.SerializerFactory;
import org.zibra.io.serialize.ToStringSerializer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class SerializerLoader {
    static {
        SerializerFactory.register(LocalDate.class, LocalDateSerializer.instance);
        SerializerFactory.register(LocalTime.class, LocalTimeSerializer.instance);
        SerializerFactory.register(LocalDateTime.class, LocalDateTimeSerializer.instance);
        SerializerFactory.register(OffsetTime.class, OffsetTimeSerializer.instance);
        SerializerFactory.register(OffsetDateTime.class, OffsetDateTimeSerializer.instance);
        SerializerFactory.register(ZonedDateTime.class, ZonedDateTimeSerializer.instance);
        SerializerFactory.register(Duration.class, ToStringSerializer.instance);
        SerializerFactory.register(Instant.class, ToStringSerializer.instance);
        SerializerFactory.register(MonthDay.class, ToStringSerializer.instance);
        SerializerFactory.register(Period.class, ToStringSerializer.instance);
        SerializerFactory.register(Year.class, ToStringSerializer.instance);
        SerializerFactory.register(YearMonth.class, ToStringSerializer.instance);
        SerializerFactory.register(ZoneId.class, ToStringSerializer.instance);
        SerializerFactory.register(ZoneOffset.class, ToStringSerializer.instance);
    }
}
