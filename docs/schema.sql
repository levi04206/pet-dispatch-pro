CREATE DATABASE IF NOT EXISTS `pet_dispatch_pro`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `pet_dispatch_pro`;

CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone` varchar(20) NOT NULL COMMENT '手机号',
    `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL(存阿里云OSS)',
    `status` tinyint DEFAULT '1' COMMENT '账号状态: 1正常 0封禁',
    `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '用户角色: USER普通用户 SITTER宠托师 ADMIN管理员',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户表';

CREATE TABLE IF NOT EXISTS `pet_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
    `user_id` bigint NOT NULL COMMENT '所属用户ID(外键逻辑)',
    `name` varchar(50) NOT NULL COMMENT '宠物名字',
    `type` tinyint NOT NULL COMMENT '种类: 1猫 2狗 3异宠',
    `breed` varchar(50) DEFAULT NULL COMMENT '品种(如: 金毛, 布偶)',
    `weight` decimal(5,2) DEFAULT NULL COMMENT '体重(kg), 影响遛狗费率',
    `is_neutered` tinyint(1) DEFAULT '0' COMMENT '是否绝育: 1是 0否',
    `aggressive_tag` tinyint(1) DEFAULT '0' COMMENT '高危攻击性标签: 1有咬人史 0温顺(重要派单依据)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物数字档案表';

CREATE TABLE IF NOT EXISTS `sitter` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '宠托师ID',
    `user_id` bigint NOT NULL COMMENT '关联的普通用户ID(身份绑定)',
    `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像(OSS URL)',
    `phone` varchar(20) NOT NULL COMMENT '联系电话',
    `id_card` varchar(20) NOT NULL COMMENT '身份证号(实名防黑产)',
    `rating` decimal(3,2) DEFAULT '5.00' COMMENT '综合信誉评分(满分5分)',
    `order_count` int DEFAULT '0' COMMENT '历史履约单量',
    `work_status` tinyint DEFAULT '0' COMMENT '工作状态: 0休息中 1接单中 2服务中',
    `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态: 0待审核 1审核通过 2审核驳回',
    `lat` decimal(10,6) DEFAULT NULL COMMENT '最新纬度(实时位置)',
    `lng` decimal(10,6) DEFAULT NULL COMMENT '最新经度(实时位置)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_phone` (`phone`),
    UNIQUE KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='W端宠托师工作档案表';

CREATE TABLE IF NOT EXISTS `orders` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
    `order_sn` varchar(64) NOT NULL COMMENT '订单流水号(全局唯一，用于支付)',
    `user_id` bigint NOT NULL COMMENT '下单用户ID',
    `pet_id` bigint NOT NULL COMMENT '宠物档案ID',
    `sitter_id` bigint DEFAULT NULL COMMENT '承接宠托师ID(抢单后填入)',
    `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
    `distance` decimal(10,2) DEFAULT NULL COMMENT '预估/实际服务距离(km)',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '订单状态: 1待支付 2待接单 3已接单(前往中) 4服务中 5服务完成 6已评价 0已取消',
    `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
    `start_proof` varchar(255) DEFAULT NULL COMMENT '开始服务图片凭证(OSS URL)',
    `end_proof` varchar(255) DEFAULT NULL COMMENT '结束服务视频/图片凭证(OSS URL)',
    `reserve_time` datetime NOT NULL COMMENT '用户预约上门时间',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `accept_time` datetime DEFAULT NULL COMMENT '宠托师接单时间',
    `start_time` datetime DEFAULT NULL COMMENT '实际开始服务时间',
    `end_time` datetime DEFAULT NULL COMMENT '服务结束时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_order_sn` (`order_sn`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sitter_id` (`sitter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核心调度订单表';
