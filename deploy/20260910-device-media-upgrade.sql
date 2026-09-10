-- 已有 monitor 数据库升级脚本：设备地点与最新上传媒体。
-- 可重复执行。请先在宝塔数据库中选择 monitor 数据库。
SET NAMES utf8mb4;
USE `monitor`;

CREATE TABLE IF NOT EXISTS `device_media` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '媒体记录主键',
    `device_code` VARCHAR(255) NOT NULL COMMENT '关联设备编码',
    `area` VARCHAR(255) NOT NULL COMMENT '上传时选择的地点',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `media_type` VARCHAR(20) NOT NULL COMMENT 'image或video',
    `media_path` VARCHAR(500) NOT NULL COMMENT '原始媒体相对地址',
    `preview_path` VARCHAR(500) DEFAULT NULL COMMENT '标注图或视频预警帧地址',
    `uploaded_time` DATETIME(6) NOT NULL COMMENT '上传分析时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_media_latest` (`device_code`(128), `uploaded_time`, `id`)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='设备上传图片视频记录';

-- 告警关联原始媒体及 AI 标注预览；动态判断列是否存在，脚本可重复执行。
SET @has_alarm_original_path = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'alarm' AND COLUMN_NAME = 'original_path'
);
SET @add_alarm_original_path = IF(
    @has_alarm_original_path = 0,
    'ALTER TABLE `alarm` ADD COLUMN `original_path` VARCHAR(500) DEFAULT NULL COMMENT ''原始上传图片或视频地址'' AFTER `detail`',
    'SELECT 1'
);
PREPARE add_alarm_original_path_stmt FROM @add_alarm_original_path;
EXECUTE add_alarm_original_path_stmt;
DEALLOCATE PREPARE add_alarm_original_path_stmt;

SET @has_alarm_preview_path = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'alarm' AND COLUMN_NAME = 'preview_path'
);
SET @add_alarm_preview_path = IF(
    @has_alarm_preview_path = 0,
    'ALTER TABLE `alarm` ADD COLUMN `preview_path` VARCHAR(500) DEFAULT NULL COMMENT ''AI标注图片或视频预警帧地址'' AFTER `original_path`',
    'SELECT 1'
);
PREPARE add_alarm_preview_path_stmt FROM @add_alarm_preview_path;
EXECUTE add_alarm_preview_path_stmt;
DEALLOCATE PREPARE add_alarm_preview_path_stmt;

INSERT INTO `device` (`code`, `name`, `type`, `area`, `status`) VALUES
    ('CAM-006', '校园道路摄像头',   '摄像头', '校园道路',   '在线'),
    ('CAM-007', '宿舍区摄像头',     '摄像头', '宿舍区',     '在线'),
    ('CAM-008', '操场入口摄像头',   '摄像头', '操场入口',   '在线'),
    ('CAM-009', '图书馆门口摄像头', '摄像头', '图书馆门口', '在线'),
    ('CAM-010', '停车场摄像头',     '摄像头', '停车场',     '在线'),
    ('CAM-011', '仓库外侧摄像头',   '摄像头', '仓库外侧',   '在线'),
    ('CAM-012', '实验楼后侧摄像头', '摄像头', '实验楼后侧', '在线'),
    ('CAM-013', '校园广场摄像头',   '摄像头', '校园广场',   '在线')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `type` = VALUES(`type`),
    `area` = VALUES(`area`),
    `status` = VALUES(`status`);
