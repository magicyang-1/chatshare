-- 创建训练任务表
CREATE TABLE training_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TRAINING',
    progress INT NOT NULL DEFAULT 0,
    current_step INT DEFAULT 0,
    total_steps INT DEFAULT 0,
    step_progress INT DEFAULT 0,
    model_type VARCHAR(100) NOT NULL,
    epochs INT NOT NULL,
    learning_rate DOUBLE NOT NULL,
    batch_size INT NOT NULL,
    start_time VARCHAR(100),
    start_timestamp BIGINT,
    duration VARCHAR(50),
    model_file_path VARCHAR(500),
    model_file_size BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_training_jobs_user_id ON training_jobs(user_id);
CREATE INDEX idx_training_jobs_status ON training_jobs(status);
CREATE INDEX idx_training_jobs_created_at ON training_jobs(created_at); 