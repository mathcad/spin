# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [spin](http://git.dev.tencent.com/mathnet/spin/).

## 2020-08-05
### spin-core [2.2.31-SNAPSHOT]
* 更新Bouncy依赖到最新版本
* 优化Http工具类，允许用户指定是否检查响应状态以适应restful风格
* 优化集合工具类，增强数组的检测方法
* 重试工具类支持在触发重试时传递上一次的异常

### spin-data [2.2.32-SNAPSHOT]
* 升级spin-data-core依赖
* 统一分页参数命名

### spin-data-core [2.2.32-SNAPSHOT]
* 升级spi-core依赖
* 分布式主键代码优化
* Page对象增加currentPage属性, 与PageRequest对象统一命明规则

### spin-cloud-spring-boot-starter [2.2.27-SNAPSHOT]
* 升级基础依赖版本
* 增加临时用户验证SPI
* 优化远程调用工具类，避免远程异常提示不正确的问题
* 优化AbstractFallback类，解决日志打印格式不正确问题

## 2020-03-27
### spin-cloud-spring-boot-starter [2.2.27-SNAPSHOT]
更新基础spin-web依赖到2.2.27-SNAPSHOT

### spin-web [2.2.27-SNAPSHOT]
修复WebExceptionHandler的拼写错误
