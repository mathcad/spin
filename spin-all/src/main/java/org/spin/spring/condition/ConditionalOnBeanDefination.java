package org.spin.spring.condition;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Arvin on 2017/2/7.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanDefinationCondition.class)
public @interface ConditionalOnBeanDefination {

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
}
