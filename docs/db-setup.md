# 数据库初始化说明

这份说明用于本地演示和交付检查，避免全新建库和旧库升级混用脚本。

## 全新本地库

全新环境直接执行完整表结构：

```sql
source docs/schema.sql;
```

如需演示数据，再执行：

```sql
source docs/demo-data.sql;
```

## 已有旧库

如果数据库已经存在旧表结构，不要重复执行 `docs/schema.sql` 覆盖判断，按时间顺序执行增量脚本：

```sql
source docs/migrations/20260413_add_orders_version.sql;
source docs/migrations/20260414_add_order_evaluation.sql;
source docs/migrations/20260414_add_user_role.sql;
```

这些 migration 是普通增量 SQL，不是可重复执行脚本；如果字段已经存在，MySQL 会报重复字段错误。

## 演示数据固定 ID

`docs/demo-data.sql` 中常用固定 ID：

- 用户：`1001`
- 宠托师用户：`1002`
- 管理员用户：`1003`
- 宠物：`2001`
- 宠托师档案：`3001`
- 待接单订单：`4001`
- 服务中订单：`4002`
- 已完成订单：`4003`
