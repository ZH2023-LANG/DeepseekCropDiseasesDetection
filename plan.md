# SpringBoot 平台化升级执行状态（2026-05-09）

## 已完成
1. 认证与权限
 - Spring Security + JWT（access/refresh）
 - refreshToken 内存存储（ConcurrentHashMap）
 - BCrypt 密码存储
 - 旧明文密码首次登录自动迁移
 - 角色能力基础（admin/common）
 - v1 认证接口：`/api/v1/auth/*`

2. 统一异常与参数校验
 - `@Valid` DTO 校验已接入 v1 接口
 - `@RestControllerAdvice` 全局异常处理
 - 统一错误码体系

3. 可观测性
 - 请求 traceId 注入与 `X-Trace-Id` 回传
 - 请求耗时日志（含用户ID）
 - Actuator + Prometheus 端点
 - `audit_log` 审计记录（登录、模型操作、任务操作、记录删除）

4. 异步任务中心
 - `async_task` 任务模型与 Mapper/Service
 - 创建任务即返回 `taskNo`
 - 任务状态：`PENDING/RUNNING/SUCCESS/FAILED/CANCELLED`
 - 任务接口：创建、查询、分页、取消、重试
 - 图片与批量图片任务可调度 Flask 并回写结果

5. 模型管理 API
 - `model_registry` 数据模型
 - 模型增删改查（含启停、默认切换、健康检查）
 - 管理员权限限制

6. 数据集与训练记录管理（后端版）
 - `dataset_import_record`、`training_job`、`training_metric`、`training_artifact` 模型
 - 数据集导入记录接口
 - 训练任务创建/查询/分页
 - 指标查询与 JSON 报告导出

7. 数据库迁移脚本
 - 新增：`CropDiseasesDetection_springboot/src/main/resources/db/migration/V1__platform_upgrade.sql`

8. 兼容策略
 - 旧接口仍可访问：`/user/**`、`/imgRecords/**`、`/videoRecords/**`、`/cameraRecords/**`、`/files/**`、`/flask/**`

## 待完善（下一迭代建议）
实验 CRUD：实验名、参数、数据集、结果、备注
对比接口：两个实验的 mAP/loss 差异
价值：从“能训练”升级到“能复盘和比较”