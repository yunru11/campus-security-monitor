-- 智能安防远程监控与告警平台 - MySQL 8 初始化脚本
-- 适用版本：MySQL 5.7/8.0、MariaDB 10.x+
-- 字符集：utf8mb4

SET NAMES utf8mb4;
SET time_zone = '+08:00';

-- 请先在宝塔面板中创建 monitor 数据库，再导入本脚本。
USE `monitor`;

-- 设备表：对应实体 edu.training.security.model.Device
CREATE TABLE IF NOT EXISTS `device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备主键',
    `code` VARCHAR(255) NOT NULL COMMENT '设备唯一编码',
    `name` VARCHAR(255) NOT NULL COMMENT '设备名称',
    `type` VARCHAR(255) NOT NULL COMMENT '设备类型',
    `area` VARCHAR(255) NOT NULL COMMENT '所属区域',
    `status` VARCHAR(255) NOT NULL COMMENT '在线状态：在线、离线',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_code` (`code`),
    KEY `idx_device_status` (`status`),
    KEY `idx_device_area_type` (`area`, `type`)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='安防设备信息';

-- 告警表：对应实体 edu.training.security.model.Alarm
CREATE TABLE IF NOT EXISTS `alarm` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警主键',
    `type` VARCHAR(255) NOT NULL COMMENT '告警类型',
    `area` VARCHAR(255) NOT NULL COMMENT '发生区域',
    `level` VARCHAR(255) NOT NULL COMMENT '告警等级：高、中、低',
    `status` VARCHAR(255) NOT NULL COMMENT '处置状态：待处置、处置中、已关闭',
    `source` VARCHAR(255) NOT NULL COMMENT '告警来源',
    `detail` VARCHAR(255) NOT NULL COMMENT '识别结果或告警详情',
    `original_path` VARCHAR(500) DEFAULT NULL COMMENT '原始上传图片或视频地址',
    `preview_path` VARCHAR(500) DEFAULT NULL COMMENT 'AI标注图片或视频预警帧地址',
    `event_time` DATETIME(6) NOT NULL COMMENT '事件发生时间',
    PRIMARY KEY (`id`),
    KEY `idx_alarm_event_time` (`event_time`),
    KEY `idx_alarm_status_event_time` (`status`, `event_time`),
    KEY `idx_alarm_source_event_time` (`source`, `event_time`),
    KEY `idx_alarm_area_event_time` (`area`, `event_time`)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='安防告警记录';

-- 设备上传媒体表：保存每台设备最近上传的原始图片或视频及标注预览。
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

-- 测试设备数据。
-- 前 3 条与 SecurityMonitorApplication.seed() 完全一致，其余为补充测试数据。
-- 固定主键配合 INSERT IGNORE，使脚本可以重复执行。
INSERT IGNORE INTO `device` (`id`, `code`, `name`, `type`, `area`, `status`) VALUES
    (1, 'CAM-001',  '西门摄像头',     '摄像头', 'A区西门',     '在线'),
    (2, 'CAM-002',  '仓库摄像头',     '摄像头', 'B区仓库',     '在线'),
    (3, 'DOOR-001', '教学楼门禁',     '门禁',   '教学楼',      '在线'),
    (4, 'CAM-003',  '南门摄像头',     '摄像头', 'A区南门',     '在线'),
    (5, 'SMOKE-001','仓库烟感探测器', '烟感',   'B区仓库',     '在线'),
    (7, 'DOOR-002', '实验楼门禁',     '门禁',   '实验楼',      '在线'),
    (8, 'CAM-005',  '教学楼南侧摄像头','摄像头','教学楼南侧', '在线');

-- 根据“校园安防测试图片”文件名补充对应监控设备。
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

-- 测试告警数据。
-- 前 3 条与 SecurityMonitorApplication.seed() 完全一致，时间也采用“当前时间减 10 天”的逻辑；
-- 其余数据用于覆盖控制器中支持的主要告警类型及三种处置状态。
INSERT IGNORE INTO `alarm`
    (`id`, `type`, `area`, `level`, `status`, `source`, `detail`, `event_time`)
VALUES
    (1,  '人员闯入', 'A区西门',    '高', '待处置', 'AI视觉分析', 'AI识别到人员进入限制区域',
        DATE_SUB(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 5 MINUTE)),
    (2,  '人员聚集', '教学楼南侧', '中', '已关闭', 'AI视觉分析', 'AI识别到人员聚集',
        DATE_SUB(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 9 MINUTE)),
    (3,  '烟火异常', 'B区仓库',    '高', '待处置', 'AI视觉分析', '检测到疑似烟火异常',
        DATE_SUB(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 13 MINUTE)),
    (4,  '车辆异常', 'A区南门',    '中', '待处置', 'AI视觉分析', 'AI识别结果：car(94%)',
        DATE_SUB(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 30 MINUTE)),
    (5,  '动物进入', 'A区西门',    '中', '已关闭', 'AI视觉分析', 'AI识别结果：dog(92%)',
        DATE_SUB(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 1 HOUR)),
    (6,  '物品异常', '教学楼',     '中', '处置中', 'AI视觉分析', 'AI识别结果：backpack(91%)',
        DATE_SUB(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 2 HOUR)),
    (7,  '人员闯入', '实验楼',     '中', '处置中', 'AI视觉分析', 'AI识别结果：person(95%)',
        DATE_SUB(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 5 HOUR)),
    (8,  '烟火异常', 'B区仓库',    '高', '已关闭', 'AI视觉分析', 'AI识别结果：smoke(97%)，现场已排查',
        DATE_SUB(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 1 DAY));
