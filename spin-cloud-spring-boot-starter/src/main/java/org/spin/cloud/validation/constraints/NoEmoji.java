package org.spin.cloud.validation.constraints;

import org.spin.cloud.validation.NoEmojiValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 不允许文本中出现Emoji表情
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */

@Constraint(validatedBy = NoEmojiValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoEmoji {

    String message() default "文本中不允许包含Emoji表情符号";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
