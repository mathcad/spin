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

package org.infrastructure.throwable;

public class SQLException extends RuntimeException {
    private static final long serialVersionUID = -6315329503841905147L;

    public static final int CANNOT_GET_CONNECTION = 0;
    public static final int SQL_EXCEPTION = 1;
    public static final int CANNOT_GET_SQL = 2;
    public static final int MAPPING_ERROR = 3;
    //UNQUE 方法需要传入主键的个数与数据库期望的主键个数不一致
    public static final int ID_EXPECTED_ONE_ERROR = 4;

    //UNQUE 方法需要传入主键的个数与数据库期望的主键个数不一致
    public static final int NOT_UNIQUE_ERROR = 5;

    //SQL 脚本运行出错
    public static final int SQL_SCRIPT_ERROR = 6;
    //期望有id，但未发现有id
    public static final int ID_NOT_FOUND = 7;


    //SQL 脚本运行出错
    public static final int TABLE_NOT_EXIST = 8;


    //根据指定类创建实例出错
    public static final int OBJECT_INSTANCE_ERROR = 9;

    //dao2 未知类型
    public static final int UNKNOW_MAPPER_SQL_TYPE = 10;

    //dao2 接口函数 参数定义错误
    public static final int ERROR_MAPPER_PARAMEER = 11;


    //dao2 接口函数 参数定义错误
    public static final int UNIQUE_EXCEPT_ERROR = 12;


    //dao2 接口函数 参数定义错误
    public static final int TAIL_CALL_ERROR = 13;

    //dao2 复合主键，未找到相应值
    public static final int ID_VALUE_ERROR = 14;

    public static final int ID_AUTOGEN_ERROR = 15;

    public static final int RESULT_NOT_FOUND = 16;

    int code;

    public SQLException(int code) {
        this.code = code;
    }

    public SQLException(int code, Exception e) {
        super(e);
        this.code = code;
    }

    public SQLException(int code, String msg, Exception e) {
        super(msg, e);
        this.code = code;
    }

    public SQLException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}

