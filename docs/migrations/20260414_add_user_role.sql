ALTER TABLE `user`
    ADD COLUMN `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '用户角色: USER普通用户 SITTER宠托师 ADMIN管理员' AFTER `status`;
