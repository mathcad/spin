/*
 *  Copyright 2002-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spin.jpa.sql.resolver;

import java.util.Map;

/**
 * 模板解析器
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 * @version 1.0
 */
@FunctionalInterface
public interface TemplateResolver {

    /**
     * 使用指定的数据解析模板，生成解析后的文本
     *
     * @param id       模板id
     * @param template 模板对象
     * @param model    数据
     * @return 解析后的文本
     */
    String resolve(String id, String template, Map<String, ?> model);
}