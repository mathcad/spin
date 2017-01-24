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

package org.spin.jpa.sql.loader;

import java.io.File;

/**
 * 文件系统SQL装载器
 * <p>实现了基于文件系统的SQL装载通用方法</p>
 * Created by xuweinan on 2016/8/14.
 *
 * @author xuweinan
 */
public abstract class FileSystemSQLLoader extends GenericSqlLoader {

    @Override
    public boolean isModified(String id) {
        File file = this.getFile(id);
        if (file == null)
            return true;
        long lastModify = file.lastModified();
        Long oldVersion = sqlSourceVersion.get(id);
        return oldVersion == null || oldVersion != lastModify;
    }

    /**
     * 根据ID与扩展名读取文件
     */
    protected abstract File getFile(String id);
}
