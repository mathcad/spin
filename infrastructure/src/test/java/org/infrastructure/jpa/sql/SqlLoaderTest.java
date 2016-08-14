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

package org.infrastructure.jpa.sql;

import org.infrastructure.jpa.core.SQLLoader;
import org.infrastructure.jpa.sql.resolver.FreemarkerResolver;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arvin on 2016/6/25.
 */
public class SqlLoaderTest {
    @Test
    public void testGetSql() {
        Map<String, String> param = new HashMap<>();
        param.put("no", "pp");
        SQLLoader loader = new ClasspathXmlLoader();
        loader.setTemplateResolver(new FreemarkerResolver());
        String template = loader.getSQL("product.findProductTarget", param).getTemplate();
        template = loader.getSQL("product.findProductTarget", param).getTemplate();
        template = loader.getSQL("product.findProductTarget", param).getTemplate();
        System.out.println(template);
        assertTrue(true);
    }
}