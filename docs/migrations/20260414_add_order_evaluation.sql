ALTER TABLE `orders`
    ADD COLUMN `evaluate_rating` tinyint DEFAULT NULL COMMENT '用户评分: 1-5' AFTER `end_proof`,
    ADD COLUMN `evaluate_content` varchar(255) DEFAULT NULL COMMENT '用户评价内容' AFTER `evaluate_rating`,
    ADD COLUMN `evaluate_time` datetime DEFAULT NULL COMMENT '用户评价时间' AFTER `evaluate_content`;
