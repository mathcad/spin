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

package org.infrastructure.jpa.sql.resolver;


import org.infrastructure.util.StringUtils;

import java.util.Map;

/**
 * 简易模板解析器，实现了最简单的变量绑定
 * <p>模板变量定义格式为${paramName}<br>所有未绑定数据的模板变量将会被移除</p>
 * Created by xuweinan on 2016/8/15.
 *
 * @author xuweinan
 * @version 1.0
 */
public class SimpleResolver implements TemplateResolver {
    @Override
    public String resolve(String id, String template, Map<String, ?> model) {
        if (StringUtils.isEmpty(template))
            return StringUtils.EMPTY;
        String result = template;
        if (null != model)
            for (Map.Entry<String, ?> param : model.entrySet()) {
                result = template.replace("${" + param.getKey() + "}", param.getValue().toString());
            }
        return result.replaceAll("\\$\\{.+}", "");
    }
}