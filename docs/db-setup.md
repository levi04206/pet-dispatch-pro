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
- 待审核申请用户：`1004`
- 宠物：`2001`
- 宠托师档案：`3001`
- 待审核宠托师档案：`3002`
- 待接单订单：`4001`
- 服务中订单：`4002`
- 已完成订单：`4003`

## 宠托师审核调试重置

如果本地反复调试后，`13800138001` 提示已经申请过，或管理员待审核列表为空，可以执行下面这段 SQL 把演示状态恢复到稳定状态：

```sql
USE `pet_dispatch_pro`;

UPDATE `user`
SET `role` = 'USER', `status` = 1, `nickname` = '演示用户'
WHERE `phone` = '13800138001';

INSERT INTO `user` (`id`, `phone`, `nickname`, `status`, `role`)
VALUES
    (1003, '13800138003', '演示管理员', 1, 'ADMIN'),
    (1004, '13800138004', '待审核申请用户', 1, 'USER')
ON DUPLICATE KEY UPDATE
    `nickname` = VALUES(`nickname`),
    `status` = VALUES(`status`),
    `role` = VALUES(`role`);

DELETE FROM `sitter`
WHERE `user_id` = 1001 OR `phone` = '13800138001';

INSERT INTO `sitter` (`id`, `user_id`, `real_name`, `phone`, `id_card`, `rating`, `order_count`, `work_status`, `audit_status`)
VALUES (3002, 1004, '王待审', '13800138004', '110101199002020022', 5.00, 0, 0, 0)
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `real_name` = VALUES(`real_name`),
    `phone` = VALUES(`phone`),
    `id_card` = VALUES(`id_card`),
    `rating` = VALUES(`rating`),
    `order_count` = VALUES(`order_count`),
    `work_status` = VALUES(`work_status`),
    `audit_status` = VALUES(`audit_status`);
```
