# 交付检查清单

用于演示或提交前快速确认核心链路可跑通。

## 代码检查

- 执行 `mvn test`，确认全部测试通过。
- 确认工作区没有未提交改动：`git status --short`。
- 确认本地提交已推送到远端：`git push origin master`。

## 本地环境

- MySQL 已创建 `pet_dispatch_pro` 数据库。
- Redis 已启动，默认地址 `127.0.0.1:6379`。
- 已复制 `src/main/resources/application-local.example.yml` 为 `application-local.yml`。
- `application-local.yml` 已填写数据库密码和 JWT 密钥。
- 如需真实上传，已填写 OSS 配置；只演示主流程时可以跳过真实 OSS 上传。

## 数据库脚本

- 全新库执行 `docs/schema.sql`。
- 旧库按顺序执行 `docs/migrations/` 下的增量脚本。
- 如需演示数据，执行 `docs/demo-data.sql`。
- 演示 ID 参考 `docs/demo-flow.md` 或 `docs/db-setup.md`。

## 核心业务演示

- 用户验证码登录成功，并返回 JWT。
- 用户可维护自己的宠物档案。
- 普通用户可申请成为宠托师。
- 管理员可查看待审核申请并审核通过。
- 宠托师可切换为接单中。
- 用户可创建订单并模拟支付。
- 审核通过且接单中的宠托师可查看公共订单池并抢单。
- 宠托师可开始服务、完成服务并提交履约凭证 URL。
- 用户可评价已完成订单。
- 用户和宠托师查询订单时只能看到各自权限范围内的数据。
