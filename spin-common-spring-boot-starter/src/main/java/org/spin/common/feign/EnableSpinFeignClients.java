/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spin.common.feign;

import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 扫描指定包下的Feign客户端接口({@link org.springframework.cloud.openfeign.FeignClient} <code>@FeignClient</code>).
 * <p>只能在Spring的配置上下文中使用({@link org.springframework.context.annotation.Configuration}
 * <code>@Configuration</code>)</p>
 *
 * @author Spencer Gibb
 * @author Dave Syer
 * @author xuweinan
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(SpinFeignClientsRegistrar.class)
public @interface EnableSpinFeignClients {

    /**
     * 与{@link #basePackages()}属性相同. 用来简化配置, 如: {@code @EnableSpinFeignClients(basePackages="org.my.pkg")}可以简单的写成
     * {@code @EnableSpinFeignClients("org.my.pkg")}.
     *
     * @return the array of 'basePackages'.
     */
    String[] value() default {};

    /**
     * 用来指定需要扫描的包。只有指定包会被Feign上下文扫描
     * <p>
     * Use {@link #basePackageClasses()} for a type-safe alternative to String-based
     * package names.
     * </p>
     *
     * @return the array of 'basePackages'.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to
     * scan for annotated components. The package of each class specified will be scanned.
     * <p>
     * Consider creating a special no-op marker class or interface in each package that
     * serves no purpose other than being referenced by this attribute.
     *
     * @return the array of 'basePackageClasses'.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * A custom <code>@Configuration</code> for all feign clients. Can contain override
     * <code>@Bean</code> definition for the pieces that make up the client, for instance
     * {@link feign.codec.Decoder}, {@link feign.codec.Encoder}, {@link feign.Contract}.
     *
     * @return list of default configurations
     * @see FeignClientsConfiguration for the defaults
     */
    Class<?>[] defaultConfiguration() default {};

    /**
     * List of classes annotated with @FeignClient. If not empty, disables classpath
     * scanning.
     *
     * @return list of FeignClient classes
     */
    Class<?>[] clients() default {};

}
