package org.spin.spring.condition;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Conditional} that only matches when the specified meta classes and/or names are
 * already contained in the {@link BeanFactory}.
 * <p>
 * The condition can only match the meta definitions that have been processed by the
 * application context so far and, as such, it is strongly recommended to use this
 * condition on auto-configuration classes only. If a candidate meta may be created by
 * another auto-configuration, make sure that the one using this condition runs after.
 *
 * @author Phillip Webb
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnBean {

    /**
     * The class type of meta that should be checked. The condition matches when any of
     * the classes specified is contained in the {@link ApplicationContext}.
     *
     * @return the class types of beans to check
     */
    Class<?>[] value() default {};

    /**
     * The class type names of meta that should be checked. The condition matches when any
     * of the classes specified is contained in the {@link ApplicationContext}.
     *
     * @return the class type names of beans to check
     */
    String[] type() default {};

    /**
     * The annotation type decorating a meta that should be checked. The condition matches
     * when any of the annotations specified is defined on a meta in the
     * {@link ApplicationContext}.
     *
     * @return the class-level annotation types to check
     */
    Class<? extends Annotation>[] annotation() default {};

    /**
     * The names of beans to check. The condition matches when any of the meta names
     * specified is contained in the {@link ApplicationContext}.
     *
     * @return the name of beans to check
     */
    String[] name() default {};

    /**
     * Strategy to decide if the application context hierarchy (parent contexts) should be
     * considered.
     *
     * @return the search strategy
     */
    SearchStrategy search() default SearchStrategy.ALL;

}