# 项目说明

这是一个宠物调度系统（pet-dispatch-pro），主要参考黑马“苍穹外卖”和“黑马点评”的业务与技术思路，面向宠物上门服务、宠托师接单、订单调度和履约存证场景。

## 当前进度

- 已完成用户模块雏形：手机号验证码登录、Redis 验证码缓存、JWT 登录态。
- 已完成宠物档案模块雏形：新增、查询、删除当前用户宠物。
- 已完成宠托师模块雏形：用户申请成为宠托师、管理员审核宠托师。
- 正在开发订单模块：创建订单、模拟支付、公共订单池、宠托师抢单、开始服务、完成服务。
- 第一轮急救修复已完成：宠物删除水平越权、抢单时 User.id 和 Sitter.id 混用。
- 第二阶段基建推进中：已引入 DTO、Spring Validation、MapStruct，先覆盖宠物新增、宠托师申请、订单创建三个写接口。
- 配置与交付基建推进中：已外置数据库、Redis、JWT 配置，新增本地配置示例和 `docs/schema.sql`。
- 第三阶段 Service 层重构已启动：宠物模块已从 Controller 直连 Mapper 调整为 Controller -> Service -> Mapper。

## 技术栈

- Spring Boot 3.2.5
- Java 21
- MyBatis-Plus
- MySQL
- Redis
- Knife4j
- Hutool
- Lombok
- 阿里云 OSS
- MapStruct
- Spring Validation

## 当前数据库

- `user`：C 端用户表。
- `pet_info`：宠物数字档案表。
- `sitter`：W 端宠托师工作档案表，当前通过 `user_id` 绑定普通用户身份。
- `orders`：核心调度订单表，当前 `sitter_id` 绑定真实的 `sitter.id`，不能绑定 `user.id`。
- 当前表结构已整理到 `docs/schema.sql`。

## 下一步目标

- 继续扩大 DTO/VO、Spring Validation、MapStruct 覆盖范围，逐步替换 Controller 直接接收 Entity 的接口。
- 继续推进 Service 层重构，下一步优先迁移宠托师申请/审核与订单状态流转逻辑。
- 第四阶段加强权限与安全：引入 Sa-Token、区分用户/宠托师/管理员权限、外置密钥和本地配置。
- 第五阶段完善交付标准：后续可在 `docs/schema.sql` 基础上接入 Flyway，补充越权删除和并发抢单测试。
