USE `pet_dispatch_pro`;

ALTER TABLE `pet_info`
    ADD COLUMN `image_url` varchar(255) DEFAULT NULL COMMENT '宠物图片URL，便于宠托师接单前判断照护难度' AFTER `weight`;

ALTER TABLE `orders`
    ADD COLUMN `target_sitter_id` bigint DEFAULT NULL COMMENT '用户指定宠托师ID，为空表示公共订单池' AFTER `sitter_id`,
    ADD COLUMN `reject_reason` varchar(100) DEFAULT NULL COMMENT '宠托师拒绝指定订单原因' AFTER `end_proof`,
    ADD COLUMN `reject_time` datetime DEFAULT NULL COMMENT '宠托师拒单时间' AFTER `end_time`,
    ADD KEY `idx_target_sitter_id` (`target_sitter_id`);
