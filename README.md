# pet-dispatch-pro

宠物上门服务调度后端 MVP，核心目标是跑通“用户下单 -> 宠托师接单 -> 履约打卡 -> 用户评价”的业务闭环。

## 核心流程

1. 用户通过手机号验证码登录，系统签发 JWT。
2. 用户维护自己的宠物档案。
3. 用户申请成为宠托师，管理员审核通过后用户角色升级为 `SITTER`。
4. 宠托师切换为接单中。
5. 用户选择自己的宠物创建订单并模拟支付。
6. 订单进入公共订单池，审核通过且接单中的宠托师可以抢单。
7. 宠托师上传履约凭证后开始服务、完成服务。
8. 用户对已完成订单进行评分和评价。

## 技术栈

- Spring Boot 3.2.5
- Java 21
- MyBatis-Plus
- MySQL
- Redis
- JWT
- MapStruct
- Spring Validation
- Knife4j
- 阿里云 OSS

## 本地配置

默认激活 `local` profile。复制 `src/main/resources/application-local.example.yml` 为 `application-local.yml`，填写本地数据库、Redis、JWT、OSS 配置。

数据库结构参考：

- `docs/schema.sql`
- `docs/migrations/`
- `docs/demo-data.sql` 提供少量演示数据，可用于本地串核心流程。
- `docs/demo-flow.md` 提供核心业务演示步骤。

## 常用接口

用户：

- `GET /api/user/sendCode?phone=...`
- `POST /api/user/login?phone=...&code=...`

宠物：

- `POST /api/pet/add`
- `GET /api/pet/list`
- `DELETE /api/pet/{id}`

宠托师：

- `POST /api/sitter/apply`
- `GET /api/sitter/me`
- `POST /api/sitter/workStatus?workStatus=0`
- `POST /api/sitter/workStatus?workStatus=1`
- `GET /api/admin/sitter/pending`
- `POST /api/admin/sitter/audit?id=...&auditStatus=1`

订单：

- `POST /api/orders/create`
- `POST /api/orders/pay?orderSn=...`
- `GET /api/orders/publicPool`
- `GET /api/orders/my`
- `GET /api/orders/sitter/my`
- `POST /api/orders/cancel?orderId=...`
- `POST /api/orders/grab?orderId=...`
- `POST /api/orders/start`
- `POST /api/orders/complete`
- `POST /api/orders/evaluate`

文件：

- `POST /api/common/upload`

## 测试

```bash
mvn test
```
