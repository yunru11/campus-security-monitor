DROP TABLE IF EXISTS alarm;
DROP TABLE IF EXISTS device_media;
DROP TABLE IF EXISTS device;

CREATE TABLE device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    area VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL
);

CREATE TABLE device_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code VARCHAR(255) NOT NULL,
    area VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    media_path VARCHAR(500) NOT NULL,
    preview_path VARCHAR(500),
    uploaded_time TIMESTAMP NOT NULL
);

CREATE TABLE alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    area VARCHAR(255) NOT NULL,
    level VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    source VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    original_path VARCHAR(500),
    preview_path VARCHAR(500),
    event_time TIMESTAMP NOT NULL
);

INSERT INTO device (code, name, type, area, status) VALUES
    ('CAM-001', '西门摄像头', '摄像头', 'A区西门', '在线'),
    ('CAM-013', '校园广场摄像头', '摄像头', '校园广场', '在线');
