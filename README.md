# DeepseekCropDiseasesDetection

## 项目简介
这是一个“农作物病虫害检测系统”，由 3 个子项目组成：
1. `CropDiseasesDetection_vue`：前端（Vue3 + Element Plus）
2. `CropDiseasesDetection_springboot`：业务后端（Spring Boot + MyBatis-Plus）
3. `CropDiseasesDetection_flask`：AI 推理后端（Flask + YOLO）

系统协作关系：
1. 前端调用 SpringBoot 提供的业务接口。
2. SpringBoot 负责用户、记录、任务、模型、训练等管理能力。
3. SpringBoot 调度 Flask 执行实际 YOLO 推理。

## 目录结构
1. `CropDiseasesDetection_vue`：前端源码
2. `CropDiseasesDetection_springboot`：后端源码
3. `CropDiseasesDetection_flask`：YOLO 推理与训练脚本
4. `图片测试`：测试图片素材
5. `视频测试`：测试视频素材

## 默认端口
1. 前端：`http://localhost:8888`
2. SpringBoot：`http://localhost:9999`
3. Flask：`http://localhost:5000`

## 快速启动（推荐顺序）
1. 启动 Flask（确保 YOLO 推理可用）。
2. 启动 SpringBoot（确保业务接口可用）。
3. 启动 Vue 前端。

## 接口与文档入口
1. SpringBoot 接口文档（最新）：`CropDiseasesDetection_springboot/接口文档.md`
2. SpringBoot 项目说明：`CropDiseasesDetection_springboot/README.md`
3. 平台化升级执行状态：`plan.md`

## 当前已实现的核心能力（后端）
1. JWT 登录认证（accessToken + refreshToken）
2. BCrypt 密码存储与旧明文密码自动迁移
3. 角色权限（admin/common）
4. 统一异常处理与错误码
5. 请求 traceId、审计日志、Actuator + Prometheus
6. 异步任务中心（任务创建、查询、取消、重试）
7. 模型管理（上传登记、启停、默认切换、健康检查）
8. 数据集导入记录与训练记录管理（任务、指标、报告）

## 注意事项
1. 当前 refreshToken 使用内存存储，SpringBoot 重启后会失效（单机方案）。
2. 当前 Flask 的视频/摄像头接口是流式形式，离线异步任务能力已预留，待 Flask 新增离线任务接口后可完全打通。
3. 生产环境请务必修改 `CropDiseasesDetection_springboot/src/main/resources/application.properties` 中数据库、JWT 密钥等配置。
