package org.spin.data.throwable;

import org.spin.core.ErrorCode;

public class SQLError extends ErrorCode {
    private static final long serialVersionUID = 6491011059653562539L;

    public static final SQLError CANNOT_GET_CONNECTION = new SQLError(150, "获取连接失败");
    public static final SQLError SQL_EXCEPTION = new SQLError(151, "SQL运行出错");
    public static final SQLError CANNOT_GET_SQL = new SQLError(152, "获取SQL出错");
    public static final SQLError MAPPING_ERROR = new SQLError(153, "Mapper映射错误");
    public static final SQLError ID_EXPECTED_ONE_ERROR = new SQLError(154, "方法需要传入主键的个数与数据库期望的主键个数不一致");
    public static final SQLError NOT_UNIQUE_ERROR = new SQLError(155, "方法需要传入主键的个数与数据库期望的主键个数不一致");
    public static final SQLError ID_NOT_FOUND = new SQLError(157, "id不存在");
    public static final SQLError TABLE_NOT_EXIST = new SQLError(158, "表不存在");
    public static final SQLError OBJECT_INSTANCE_ERROR = new SQLError(159, "根据指定类创建实例出错");
    public static final SQLError UNKNOW_MAPPER_SQL_TYPE = new SQLError(160, "未知映射类型");
    public static final SQLError ERROR_MAPPER_PARAMEER = new SQLError(161, "Mapper映射参数错误");
    public static final SQLError UNIQUE_EXCEPT_ERROR = new SQLError(162, "唯一性验证错误");
    public static final SQLError ID_AUTOGEN_ERROR = new SQLError(163, "主键生成失败");
    public static final SQLError RESULT_NOT_FOUND = new SQLError(164, "结果不存在");
    public static final SQLError WRITE_NOT_PERMISSION = new SQLError(164, "数据源不允许写入");

    public SQLError(int value, String desc) {
        super(value, desc);
    }
}
