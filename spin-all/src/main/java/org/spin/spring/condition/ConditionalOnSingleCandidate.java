package org.spin.spring.condition;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Conditional} that only matches when the specified meta class is already
 * contained in the {@link BeanFactory} and a single candidate can be determined.
 * <p>
 * The condition will also match if multiple matching meta instances are already contained
 * in the {@link BeanFactory} but a primary candidate has been defined; essentially, the
 * condition match if auto-wiring a meta with the defined type will succeed.
 * <p>
 * The condition can only match the meta definitions that have been processed by the
 * application context so far and, as such, it is strongly recommended to use this
 * condition on auto-configuration classes only. If a candidate meta may be created by
 * another auto-configuration, make sure that the one using this condition runs after.
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnSingleCandidate {

    /**
     * The class type of meta that should be checked. The condition match if the class
     * specified is contained in the {@link ApplicationContext} and a primary candidate
     * exists in case of multiple instances.
     * <p>
     * This attribute may <strong>not</strong> be used in conjunction with {@link #type()}
     * , but it may be used instead of {@link #type()}.
     *
     * @return the class type of the meta to check
     */
    Class<?> value() default Object.class;

    /**
     * The class type name of meta that should be checked. The condition matches if the
     * class specified is contained in the {@link ApplicationContext} and a primary
     * candidate exists in case of multiple instances.
     * <p>
     * This attribute may <strong>not</strong> be used in conjunction with
     * {@link #value()}, but it may be used instead of {@link #value()}.
     *
     * @return the class type name of the meta to check
     */
    String type() default "";

    /**
     * Strategy to decide if the application context hierarchy (parent contexts) should be
     * considered.
     *
     * @return the search strategy
     */
    SearchStrategy search() default SearchStrategy.ALL;

}
