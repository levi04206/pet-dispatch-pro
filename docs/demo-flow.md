# 核心流程演示步骤

这份文档只覆盖项目主线业务，不展开复杂后台 CRUD。

## 1. 初始化数据

先执行表结构：

```sql
source docs/schema.sql;
```

如果需要演示数据，再执行：

```sql
source docs/demo-data.sql;
```

## 2. 用户登录

1. `GET /api/user/sendCode?phone=13800138001`
2. 从控制台日志复制验证码。
3. `POST /api/user/login?phone=13800138001&code=验证码`
4. 后续请求带请求头：`token: 登录返回的 JWT`

演示重点：验证码登录、Redis 缓存、JWT 登录态。

## 3. 宠物档案

1. `POST /api/pet/add`
2. `GET /api/pet/list`
3. `DELETE /api/pet/{id}`

演示重点：宠物数据绑定当前用户，删除和查询都按当前用户隔离。

## 4. 宠托师入驻

1. 普通用户调用 `POST /api/sitter/apply`
2. 管理员调用 `GET /api/admin/sitter/pending` 查看待审核申请。
3. 管理员调用 `POST /api/admin/sitter/audit?id=宠托师ID&auditStatus=1`
4. 宠托师调用 `GET /api/sitter/me`
5. 宠托师调用 `POST /api/sitter/workStatus?workStatus=1`

演示重点：申请、管理员审核、角色升级、切换接单状态。

## 5. 订单调度

1. 用户调用 `POST /api/orders/create`
2. 用户调用 `POST /api/orders/pay?orderSn=订单号`
3. 宠托师调用 `GET /api/orders/publicPool`
4. 宠托师调用 `POST /api/orders/grab?orderId=订单ID`
5. 宠托师上传凭证：`POST /api/common/upload`
6. 宠托师调用 `POST /api/orders/start`
7. 宠托师再次上传完成凭证。
8. 宠托师调用 `POST /api/orders/complete`
9. 用户调用 `POST /api/orders/evaluate`

演示重点：公共订单池、抢单、履约打卡、评价闭环。

## 6. 查询和取消

- 用户查询自己的订单：`GET /api/orders/my`
- 用户查询自己的订单详情：`GET /api/orders/{orderId}`
- 宠托师查询自己承接的订单：`GET /api/orders/sitter/my`
- 宠托师查询自己承接的订单详情：`GET /api/orders/sitter/{orderId}`
- 用户取消待支付/待接单订单：`POST /api/orders/cancel?orderId=订单ID`

演示重点：订单查询按身份隔离，取消订单按状态机限制。
