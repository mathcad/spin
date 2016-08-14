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

import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 基于Markdown语法的sql装载器
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 */
public class ClasspathMdLoader extends FileSystemSQLLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClasspathMdLoader.class);

    @Override
    public String getSqlTemplateSrc(String id) {
        // TODO: 2016/8/14 实现markdown语法的解析
        return null;
    }

    @Override
    protected File getFile(String id) {
        String cmdFileName = id.substring(0, id.lastIndexOf("."));
        String path = "/" + (StringUtils.isEmpty(this.getRootUri()) ? "" : (this.getRootUri() + "/")) + cmdFileName + ".md";
        String uri;
        uri = this.getClass().getResource(path).getPath();
        return new File(uri);
    }
}