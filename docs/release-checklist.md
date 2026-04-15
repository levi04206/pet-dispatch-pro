# 交付检查清单

用于演示或提交前快速确认核心链路可跑通。

## 代码检查

- 执行 `mvn test`，确认全部测试通过。
- 执行 `git status --short`，确认工作区没有未提交改动。
- 执行 `git push origin master`，确认本地提交已推送到远端。

## 本地环境

- MySQL 已创建 `pet_dispatch_pro` 数据库。
- Redis 已启动，默认地址 `127.0.0.1:6379`。
- 已复制 `src/main/resources/application-local.example.yml` 为 `application-local.yml`。
- `application-local.yml` 已填写本地数据库密码和 JWT 密钥。
- 如需真实上传，填写 OSS 配置；只演示主流程时可以先跳过真实 OSS 上传。

## 数据库脚本

- 全新数据库执行 `docs/schema.sql`。
- 旧数据库按顺序执行 `docs/migrations/` 下的增量脚本。
- 如需演示数据，执行 `docs/demo-data.sql`。
- 演示固定 ID 参考 `docs/demo-flow.md` 或 `docs/db-setup.md`。

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

## 安全收口

- 确认 `GET /api/user/test` 调试写接口不存在。
- 确认 `application-local.example.yml` 仅包含占位符，不提交真实数据库、JWT、OSS 密钥。
- 确认未知异常返回统一兜底文案，不直接暴露内部异常细节。
- 确认无效 token 会清理 `BaseContext`。
- 确认管理员接口在缺少管理员角色时返回 403。

## 最终资料

- `README.md`：项目说明、技术栈、本地配置、常用接口。
- `docs/demo-flow.md`：5 分钟核心演示流程。
- `docs/final-summary.md`：最终交付摘要。

