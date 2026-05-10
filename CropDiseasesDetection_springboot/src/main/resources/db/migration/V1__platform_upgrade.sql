-- 平台化升级：安全、治理、可观测、异步任务、模型、训练
-- 注意：本脚本为增量脚本，执行前请先在测试环境验证。

ALTER TABLE user
    ADD COLUMN password_upgraded_at DATETIME NULL COMMENT '密码升级为BCrypt时间';

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_id INT NULL,
    actor_role VARCHAR(32) NULL,
    action VARCHAR(128) NOT NULL,
    target_type VARCHAR(64) NULL,
    target_id VARCHAR(128) NULL,
    detail_json TEXT NULL,
    trace_id VARCHAR(64) NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_audit_actor_created (actor_id, created_at),
    INDEX idx_audit_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';

CREATE TABLE IF NOT EXISTS async_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_no VARCHAR(64) NOT NULL,
    task_type VARCHAR(64) NOT NULL,
    owner_id INT NOT NULL,
    request_json MEDIUMTEXT NULL,
    result_json MEDIUMTEXT NULL,
    status VARCHAR(32) NOT NULL,
    error_code VARCHAR(64) NULL,
    error_message TEXT NULL,
    progress INT NOT NULL DEFAULT 0,
    started_at DATETIME NULL,
    finished_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_task_no (task_no),
    INDEX idx_owner_created (owner_id, created_at),
    INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步任务中心';

CREATE TABLE IF NOT EXISTS model_registry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_name VARCHAR(128) NOT NULL,
    version VARCHAR(64) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT NULL,
    sha256 VARCHAR(128) NULL,
    framework_type VARCHAR(16) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    remark VARCHAR(512) NULL,
    health_status VARCHAR(32) NULL,
    health_message VARCHAR(512) NULL,
    created_by INT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_model_name_version (model_name, version),
    INDEX idx_model_default (is_default),
    INDEX idx_model_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型注册表';

CREATE TABLE IF NOT EXISTS dataset_import_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dataset_name VARCHAR(128) NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    source_path VARCHAR(512) NOT NULL,
    sample_count INT NOT NULL DEFAULT 0,
    class_count INT NOT NULL DEFAULT 0,
    imported_by INT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_dataset_created (dataset_name, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集导入记录';

CREATE TABLE IF NOT EXISTS training_job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_no VARCHAR(64) NOT NULL,
    job_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    owner_id INT NOT NULL,
    dataset_name VARCHAR(128) NOT NULL,
    train_params_json MEDIUMTEXT NULL,
    result_summary_json MEDIUMTEXT NULL,
    error_message TEXT NULL,
    started_at DATETIME NULL,
    finished_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_training_job_no (job_no),
    INDEX idx_training_owner_created (owner_id, created_at),
    INDEX idx_training_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练任务';

CREATE TABLE IF NOT EXISTS training_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_no VARCHAR(64) NOT NULL,
    epoch_no INT NOT NULL,
    train_loss DOUBLE NULL,
    val_loss DOUBLE NULL,
    map50 DOUBLE NULL,
    map5095 DOUBLE NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_metric_job_epoch (job_no, epoch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练指标';

CREATE TABLE IF NOT EXISTS training_artifact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_no VARCHAR(64) NOT NULL,
    artifact_type VARCHAR(32) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    sha256 VARCHAR(128) NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_artifact_job (job_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练产物';
