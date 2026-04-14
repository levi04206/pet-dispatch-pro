USE `pet_dispatch_pro`;

INSERT INTO `user` (`id`, `phone`, `nickname`, `status`, `role`)
VALUES
    (1001, '13800138001', '演示用户', 1, 'USER'),
    (1002, '13800138002', '演示宠托师', 1, 'SITTER'),
    (1003, '13800138003', '演示管理员', 1, 'ADMIN')
ON DUPLICATE KEY UPDATE
    `nickname` = VALUES(`nickname`),
    `status` = VALUES(`status`),
    `role` = VALUES(`role`);

INSERT INTO `pet_info` (`id`, `user_id`, `name`, `type`, `breed`, `weight`)
VALUES
    (2001, 1001, '小福', 2, '柯基', 10.50)
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `name` = VALUES(`name`),
    `type` = VALUES(`type`),
    `breed` = VALUES(`breed`),
    `weight` = VALUES(`weight`);

INSERT INTO `sitter` (`id`, `user_id`, `real_name`, `phone`, `id_card`, `rating`, `order_count`, `work_status`, `audit_status`)
VALUES
    (3001, 1002, '张三', '13800138002', '110101199001010011', 5.00, 0, 1, 1)
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `real_name` = VALUES(`real_name`),
    `phone` = VALUES(`phone`),
    `id_card` = VALUES(`id_card`),
    `rating` = VALUES(`rating`),
    `order_count` = VALUES(`order_count`),
    `work_status` = VALUES(`work_status`),
    `audit_status` = VALUES(`audit_status`);

INSERT INTO `orders` (`id`, `order_sn`, `user_id`, `pet_id`, `sitter_id`, `total_amount`, `pay_amount`, `distance`, `status`, `version`, `reserve_time`, `pay_time`, `accept_time`, `start_time`, `end_time`, `start_proof`, `end_proof`)
VALUES
    (4001, 'OD_DEMO_PENDING_ACCEPT', 1001, 2001, NULL, 99.00, 99.00, 3.50, 2, 0, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), NULL, NULL, NULL, NULL, NULL),
    (4002, 'OD_DEMO_IN_SERVICE', 1001, 2001, 3001, 99.00, 99.00, 3.50, 4, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), NOW(), NOW(), NULL, 'https://example.com/start.jpg', NULL),
    (4003, 'OD_DEMO_COMPLETED', 1001, 2001, 3001, 99.00, 99.00, 3.50, 5, 3, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), NOW(), NOW(), NOW(), 'https://example.com/start.jpg', 'https://example.com/end.jpg')
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `version` = VALUES(`version`),
    `sitter_id` = VALUES(`sitter_id`),
    `start_proof` = VALUES(`start_proof`),
    `end_proof` = VALUES(`end_proof`);
