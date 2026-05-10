# CropDiseasesDetection SpringBoot 后端说明

## 1. 项目定位
本服务是系统的业务后端，负责：
1. 用户与权限管理
2. 检测记录管理
3. 异步任务管理
4. 模型注册与切换
5. 数据集导入与训练记录管理
6. 调度 Flask 执行 YOLO 推理

## 2. 技术栈
1. Java 8
2. Spring Boot 2.3.7
3. Spring Security + JWT
4. MyBatis-Plus
5. MySQL
6. Actuator + Prometheus

## 3. 运行前准备
1. 安装并启动 MySQL（准备数据库 `yoloai`）。
2. 检查配置文件：`src/main/resources/application.properties`
3. 执行数据库脚本：`src/main/resources/db/migration/V1__platform_upgrade.sql`
4. 确保 Flask 已启动：`http://127.0.0.1:5000`

## 4. 启动命令
```bash
mvn spring-boot:run
```

默认端口：`9999`

## 5. 接口文档
1. 完整接口文档：`接口文档.md`
2. 新版接口前缀：`/api/v1`
3. 旧版兼容接口仍可用：`/user/**`、`/imgRecords/**`、`/videoRecords/**`、`/cameraRecords/**`、`/files/**`、`/flask/**`

## 6. 已实现能力
1. 认证与权限
- JWT 双 token（access + refresh）
- BCrypt 密码存储
- 旧明文密码登录自动迁移
- 角色权限（admin/common）

2. 统一治理
- `@Valid` 参数校验
- 全局异常处理
- 统一错误码响应

3. 可观测性
- 请求 traceId + 响应头 `X-Trace-Id`
- 请求耗时日志
- 审计日志（`audit_log`）
- Actuator/Prometheus 监控端点

4. 异步任务中心
- 图片与批量图片任务：支持创建并后台执行
- 任务状态：`PENDING/RUNNING/SUCCESS/FAILED/CANCELLED`
- 任务操作：创建、查询、分页、取消、重试

5. 模型管理
- 模型登记、修改、启停、默认切换、健康检查

6. 数据集与训练记录
- 数据集导入记录
- 训练任务记录、指标查询、报告导出

7. 训练实验管理（新增）
- 训练实验 CRUD（实验名、参数、数据集、结果、备注）
- 实验核心指标记录（`map50/map5095/trainLoss/valLoss`）
- 实验对比接口（两个实验的 mAP/loss 差异）

## 7. 运行限制
1. refreshToken 目前使用内存存储，服务重启后失效。
2. 当前 Flask 视频/摄像头是流式接口，离线异步视频任务能力已预留，待 Flask 增加任务型接口后可完全打通。

## 8. 下一步建议
1. 引入 Redis 管理 refreshToken（支持多实例）。
2. 增加 OpenAPI/Swagger 自动文档。
3. 增加训练任务执行器与回调机制，实现训练全流程自动化。
4. 可选增加“实验版本标签 + 最佳实验推荐”能力，进一步提升复盘效率。
