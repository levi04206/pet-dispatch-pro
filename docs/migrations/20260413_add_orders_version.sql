USE `pet_dispatch_pro`;

ALTER TABLE `orders`
    ADD COLUMN `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号' AFTER `status`;
