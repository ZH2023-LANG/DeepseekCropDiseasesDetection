# DeepseekCropDiseasesDetection

## 项目简介
本项目是一个“农作物病虫害检测系统”，由三部分组成：

- `CropDiseasesDetection_vue`：前端（Vue3 + Element Plus）
- `CropDiseasesDetection_springboot`：业务后端（Spring Boot + MyBatis-Plus）
- `CropDiseasesDetection_flask`：AI 推理后端（Flask + YOLO + Socket.IO）

## 运行端口（默认）
- 前端：`http://localhost:8888`
- Spring Boot：`http://localhost:9999`
- Flask：`http://localhost:5000`

## 接口入口说明
- 业务与数据管理接口：`http://localhost:9999`
- AI 推理接口（图片/视频/摄像头）：`http://localhost:5000`
- 前端开发代理：
  - `/api/*` -> `http://localhost:9999/*`
  - `/flask/*` -> `http://localhost:5000/*`

## 已实现的主要接口分组
- 用户管理：`/user`
- 图片记录：`/imgRecords`
- 视频记录：`/videoRecords`
- 摄像头记录：`/cameraRecords`
- 文件上传下载：`/files`
- AI 预测聚合（Spring 转发 Flask）：`/flask`
- Flask 原生预测接口：`/predictImg`、`/predictImgBatch`、`/predictVideo`、`/predictCamera`、`/stopCamera`、`/file_names`

## 说明
- 本 README 为根目录补充文档，便于快速定位项目结构与接口入口。
- 详细 RESTful 接口字段、请求示例、返回示例、错误码请以本次对话输出的接口文档为准。
