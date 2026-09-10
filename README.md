# 校园安防智能监控平台

基于 Spring Boot、Vue 3、MyBatis-Plus、MySQL 和 WebSocket 构建的校园安防业务系统。

本仓库提供业务系统源码与视觉识别接口，不包含 YOLO 模型、权重文件和训练资源。

## 主要功能

- 后端登录与接口访问控制
- 设备信息新增、修改、删除和状态查询
- 图片及视频识别结果接入
- 实时告警、处置中和已关闭状态流转
- WebSocket 告警类型实时推送，空类型不推送
- 告警原图和标注预览查看
- 监控大屏、设备最新画面和摄像头虚拟地图
- 首页及监控大屏待处置告警提醒

## 技术栈

- Java 17
- Spring Boot 3.3.4
- Spring Security
- MyBatis-Plus 3.5.7
- MySQL 5.7、MySQL 8.0 或 MariaDB 10.x+
- Vue 3
- WebSocket

## 数据库初始化

1. 创建名为 `monitor` 的数据库。
2. 导入 `deploy/mysql8-monitor.sql`。
3. 根据实际数据库信息配置环境变量。

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=monitor
DB_USERNAME=monitor_user
DB_PASSWORD=请填写数据库密码
```

## 登录及接口配置

生产环境请设置以下环境变量，不要继续使用源码中的默认值。

```text
MONITOR_ADMIN_USERNAME=请填写管理员账号
MONITOR_ADMIN_PASSWORD=请填写高强度密码
AI_EVENT_TOKEN=请填写与视觉服务一致的接口令牌
```

## 本地运行

Windows：

```powershell
.\mvnw.cmd spring-boot:run
```

Linux 或 macOS：

```bash
./mvnw spring-boot:run
```

启动后访问 `http://localhost:8888/login.html`。

## 测试

```powershell
.\mvnw.cmd test
```

当前项目包含登录鉴权、设备管理、媒体记录、告警、AI 事件和 WebSocket 等自动化测试。

## 视觉服务接口

视觉服务调用 `POST /api/ai/event` 上报识别结果，并在请求头中携带 `X-AI-Token`。后端负责保存媒体记录、生成业务告警并向页面推送非空告警类型。
