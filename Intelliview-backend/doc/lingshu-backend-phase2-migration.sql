-- 第二阶段增量迁移 SQL
-- 用途：将已有的灵枢数据库从旧结构升级到第二阶段统一数据库底座
-- 注意：本脚本不会 DROP 旧表，适合已有数据库增量升级
-- 建议执行前先完整备份数据库

SET @db = DATABASE();

-- ===========================
-- 1. 通用辅助过程
-- ===========================

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE add_index_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_index_name VARCHAR(128),
    IN p_index_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = @db
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = p_index_sql;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS run_sql_if_column_exists;
DELIMITER $$
CREATE PROCEDURE run_sql_if_column_exists(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_sql TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = p_sql;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

-- ===========================
-- 2. 扩展保留表
-- ===========================

-- 2.1 users
CALL add_column_if_missing('users', 'target_job_role_id', 'BIGINT NULL AFTER `experience`');
CALL add_column_if_missing('users', 'experience_level', 'VARCHAR(30) NULL AFTER `target_job_role_id`');
CALL add_column_if_missing('users', 'preferred_company_type', 'VARCHAR(50) NULL AFTER `experience_level`');
CALL add_column_if_missing('users', 'resume_summary', 'TEXT NULL AFTER `preferred_company_type`');
CALL add_column_if_missing('users', 'latest_overall_score', 'DECIMAL(5,2) NOT NULL DEFAULT 0.00 AFTER `resume_summary`');
CALL add_column_if_missing('users', 'latest_job_match_score', 'DECIMAL(5,2) NOT NULL DEFAULT 0.00 AFTER `latest_overall_score`');

UPDATE users
SET experience_level = COALESCE(experience_level, 'CAMPUS'),
    latest_overall_score = COALESCE(latest_overall_score, 0.00),
    latest_job_match_score = COALESCE(latest_job_match_score, 0.00);

-- 2.2 questions
CALL add_column_if_missing('questions', 'question_type', 'VARCHAR(50) NOT NULL DEFAULT ''TECHNICAL'' AFTER `category_id`');
CALL add_column_if_missing('questions', 'primary_job_role_id', 'BIGINT NULL AFTER `question_type`');
CALL add_column_if_missing('questions', 'skill_dimension_summary', 'VARCHAR(500) NULL AFTER `primary_job_role_id`');
CALL add_column_if_missing('questions', 'standard_answer_points', 'JSON NULL AFTER `skill_dimension_summary`');
CALL add_column_if_missing('questions', 'common_mistakes', 'JSON NULL AFTER `standard_answer_points`');
CALL add_column_if_missing('questions', 'follow_up_prompts', 'JSON NULL AFTER `common_mistakes`');
CALL add_column_if_missing('questions', 'scoring_points', 'JSON NULL AFTER `follow_up_prompts`');
CALL add_column_if_missing('questions', 'recommended_resources', 'JSON NULL AFTER `scoring_points`');
CALL add_column_if_missing('questions', 'is_for_interview', 'BOOLEAN NOT NULL DEFAULT TRUE AFTER `recommended_resources`');
CALL add_column_if_missing('questions', 'is_for_practice', 'BOOLEAN NOT NULL DEFAULT TRUE AFTER `is_for_interview`');
CALL add_column_if_missing('questions', 'interview_frequency', 'INT NOT NULL DEFAULT 0 AFTER `is_for_practice`');
CALL add_column_if_missing('questions', 'source_type', 'VARCHAR(30) NOT NULL DEFAULT ''ADMIN'' AFTER `interview_frequency`');

UPDATE questions
SET question_type = COALESCE(question_type, 'TECHNICAL'),
    is_for_interview = COALESCE(is_for_interview, TRUE),
    is_for_practice = COALESCE(is_for_practice, TRUE),
    interview_frequency = COALESCE(interview_frequency, 0),
    source_type = COALESCE(source_type, 'ADMIN');

CALL add_index_if_missing(
    'questions',
    'idx_primary_job_role_id',
    'ALTER TABLE `questions` ADD INDEX `idx_primary_job_role_id` (`primary_job_role_id`)'
);
CALL add_index_if_missing(
    'questions',
    'idx_question_type',
    'ALTER TABLE `questions` ADD INDEX `idx_question_type` (`question_type`)'
);

-- 2.3 user_practice_history
CALL add_column_if_missing('user_practice_history', 'job_role_id', 'BIGINT NULL AFTER `question_id`');
CALL add_column_if_missing('user_practice_history', 'question_type', 'VARCHAR(50) NULL AFTER `job_role_id`');
CALL add_column_if_missing('user_practice_history', 'self_score', 'DECIMAL(5,2) NULL AFTER `duration`');
CALL add_column_if_missing('user_practice_history', 'system_score', 'DECIMAL(5,2) NULL AFTER `self_score`');
CALL add_column_if_missing('user_practice_history', 'weakness_tags', 'JSON NULL AFTER `system_score`');
CALL add_column_if_missing('user_practice_history', 'practice_source', 'VARCHAR(50) NOT NULL DEFAULT ''DIRECT'' AFTER `weakness_tags`');
CALL add_column_if_missing('user_practice_history', 'linked_interview_id', 'BIGINT NULL AFTER `practice_source`');

UPDATE user_practice_history
SET practice_source = COALESCE(practice_source, 'DIRECT');

CALL add_index_if_missing(
    'user_practice_history',
    'idx_job_role_id',
    'ALTER TABLE `user_practice_history` ADD INDEX `idx_job_role_id` (`job_role_id`)'
);

-- 2.4 ai_interviews
CALL add_column_if_missing('ai_interviews', 'job_role_id', 'BIGINT NULL AFTER `title`');
CALL add_column_if_missing('ai_interviews', 'target_position', 'VARCHAR(100) NULL AFTER `job_role_id`');
CALL add_column_if_missing('ai_interviews', 'actual_duration', 'INT NOT NULL DEFAULT 0 AFTER `duration`');
CALL add_column_if_missing('ai_interviews', 'total_score', 'DECIMAL(5,2) NOT NULL DEFAULT 0.00 AFTER `actual_duration`');
CALL add_column_if_missing('ai_interviews', 'difficulty', 'VARCHAR(20) NULL DEFAULT ''MEDIUM'' AFTER `total_score`');
CALL add_column_if_missing('ai_interviews', 'skill_tags', 'JSON NULL AFTER `difficulty`');
CALL add_column_if_missing('ai_interviews', 'experience_level', 'VARCHAR(30) NULL AFTER `skill_tags`');
CALL add_column_if_missing('ai_interviews', 'company_type', 'VARCHAR(50) NULL AFTER `experience_level`');
CALL add_column_if_missing('ai_interviews', 'voice_enabled', 'BOOLEAN DEFAULT FALSE AFTER `company_type`');
CALL add_column_if_missing('ai_interviews', 'project_summary', 'TEXT NULL AFTER `voice_enabled`');
CALL add_column_if_missing('ai_interviews', 'template_code', 'VARCHAR(50) NULL AFTER `project_summary`');
CALL add_column_if_missing('ai_interviews', 'assessment_focus', 'JSON NULL AFTER `template_code`');
CALL add_column_if_missing('ai_interviews', 'config_snapshot', 'JSON NULL AFTER `assessment_focus`');
CALL add_column_if_missing('ai_interviews', 'question_count', 'INT NOT NULL DEFAULT 0 AFTER `config_snapshot`');
CALL add_column_if_missing('ai_interviews', 'started_at', 'DATETIME NULL AFTER `updated_at`');
CALL add_column_if_missing('ai_interviews', 'ended_at', 'DATETIME NULL AFTER `started_at`');

UPDATE ai_interviews
SET target_position = COALESCE(target_position, title),
    actual_duration = COALESCE(actual_duration, duration, 0),
    total_score = COALESCE(total_score, 0.00),
    difficulty = COALESCE(difficulty, 'MEDIUM'),
    experience_level = COALESCE(experience_level, 'CAMPUS'),
    question_count = COALESCE(question_count, 0);

CALL run_sql_if_column_exists(
    'ai_interviews',
    'total_questions',
    'UPDATE ai_interviews SET question_count = COALESCE(question_count, total_questions, 0)'
);

CALL add_index_if_missing(
    'ai_interviews',
    'idx_job_role_id',
    'ALTER TABLE `ai_interviews` ADD INDEX `idx_job_role_id` (`job_role_id`)'
);
CALL add_index_if_missing(
    'ai_interviews',
    'idx_target_position',
    'ALTER TABLE `ai_interviews` ADD INDEX `idx_target_position` (`target_position`)'
);

-- 2.5 ai_interview_questions
CALL add_column_if_missing('ai_interview_questions', 'content', 'TEXT NULL AFTER `interview_id`');
CALL add_column_if_missing('ai_interview_questions', 'type', 'VARCHAR(50) NULL AFTER `content`');
CALL add_column_if_missing('ai_interview_questions', 'topic', 'VARCHAR(100) NULL AFTER `difficulty`');
CALL add_column_if_missing('ai_interview_questions', 'estimated_time', 'INT NOT NULL DEFAULT 120 AFTER `topic`');
CALL add_column_if_missing('ai_interview_questions', 'question_order', 'INT NOT NULL DEFAULT 0 AFTER `estimated_time`');
CALL add_column_if_missing('ai_interview_questions', 'context', 'JSON NULL AFTER `question_order`');
CALL add_column_if_missing('ai_interview_questions', 'source_question_id', 'BIGINT NULL AFTER `context`');
CALL add_column_if_missing('ai_interview_questions', 'parent_question_id', 'BIGINT NULL AFTER `source_question_id`');

CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'question_text',
    'ALTER TABLE `ai_interview_questions` MODIFY COLUMN `question_text` TEXT NULL'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'question_type',
    'ALTER TABLE `ai_interview_questions` MODIFY COLUMN `question_type` VARCHAR(50) NULL'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'sort_order',
    'ALTER TABLE `ai_interview_questions` MODIFY COLUMN `sort_order` INT NULL'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'time_limit',
    'ALTER TABLE `ai_interview_questions` MODIFY COLUMN `time_limit` INT NULL'
);

UPDATE ai_interview_questions
SET content = COALESCE(content, question_text),
    type = COALESCE(type, question_type, 'INITIAL'),
    estimated_time = COALESCE(estimated_time, 120),
    question_order = COALESCE(question_order, 0);

CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'question_text',
    'UPDATE ai_interview_questions SET content = COALESCE(content, question_text)'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'question_type',
    'UPDATE ai_interview_questions SET type = COALESCE(type, question_type, ''INITIAL'')'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'time_limit',
    'UPDATE ai_interview_questions SET estimated_time = COALESCE(estimated_time, time_limit, 120)'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'sort_order',
    'UPDATE ai_interview_questions SET question_order = COALESCE(question_order, sort_order, 0)'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'question_text',
    'UPDATE ai_interview_questions SET question_text = COALESCE(question_text, content)'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'question_type',
    'UPDATE ai_interview_questions SET question_type = COALESCE(question_type, type)'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'sort_order',
    'UPDATE ai_interview_questions SET sort_order = COALESCE(sort_order, question_order)'
);
CALL run_sql_if_column_exists(
    'ai_interview_questions',
    'time_limit',
    'UPDATE ai_interview_questions SET time_limit = COALESCE(time_limit, estimated_time, 120)'
);

CALL add_index_if_missing(
    'ai_interview_questions',
    'idx_source_question_id',
    'ALTER TABLE `ai_interview_questions` ADD INDEX `idx_source_question_id` (`source_question_id`)'
);

-- 2.6 ai_interview_answers
CALL add_column_if_missing('ai_interview_answers', 'content', 'TEXT NULL AFTER `question_id`');
CALL add_column_if_missing('ai_interview_answers', 'transcript_text', 'TEXT NULL AFTER `audio_url`');
CALL add_column_if_missing('ai_interview_answers', 'input_mode', 'VARCHAR(20) NOT NULL DEFAULT ''TEXT'' AFTER `transcript_text`');
CALL add_column_if_missing('ai_interview_answers', 'confidence_level', 'INT NOT NULL DEFAULT 0 AFTER `duration`');
CALL add_column_if_missing('ai_interview_answers', 'asr_confidence', 'DECIMAL(5,2) NULL AFTER `confidence_level`');
CALL add_column_if_missing('ai_interview_answers', 'speaking_rate', 'DECIMAL(8,2) NULL AFTER `asr_confidence`');
CALL add_column_if_missing('ai_interview_answers', 'pause_count', 'INT NOT NULL DEFAULT 0 AFTER `speaking_rate`');
CALL add_column_if_missing('ai_interview_answers', 'clarity_score', 'DECIMAL(5,2) NULL AFTER `pause_count`');
CALL add_column_if_missing('ai_interview_answers', 'expression_confidence', 'DECIMAL(5,2) NULL AFTER `clarity_score`');
CALL add_column_if_missing('ai_interview_answers', 'emotion_label', 'VARCHAR(50) NULL AFTER `expression_confidence`');
CALL add_column_if_missing('ai_interview_answers', 'score', 'DECIMAL(5,2) NOT NULL DEFAULT 0.00 AFTER `confidence_level`');
CALL add_column_if_missing('ai_interview_answers', 'feedback', 'JSON NULL AFTER `score`');
CALL add_column_if_missing('ai_interview_answers', 'keyword_analysis', 'JSON NULL AFTER `feedback`');
CALL add_column_if_missing('ai_interview_answers', 'expression_analysis', 'JSON NULL AFTER `keyword_analysis`');
CALL add_column_if_missing('ai_interview_answers', 'submitted_at', 'DATETIME NULL AFTER `expression_analysis`');

UPDATE ai_interview_answers
SET content = COALESCE(content, content),
    transcript_text = COALESCE(transcript_text, transcript_text),
    confidence_level = COALESCE(confidence_level, 0),
    score = COALESCE(score, 0.00),
    submitted_at = COALESCE(submitted_at, NOW());

CALL run_sql_if_column_exists(
    'ai_interview_answers',
    'answer_text',
    'UPDATE ai_interview_answers SET content = COALESCE(content, answer_text), transcript_text = COALESCE(transcript_text, answer_text)'
);
CALL run_sql_if_column_exists(
    'ai_interview_answers',
    'created_at',
    'UPDATE ai_interview_answers SET submitted_at = COALESCE(submitted_at, created_at, NOW())'
);

CALL add_index_if_missing(
    'ai_interview_answers',
    'idx_submitted_at',
    'ALTER TABLE `ai_interview_answers` ADD INDEX `idx_submitted_at` (`submitted_at`)'
);

-- 2.7 ai_interview_assessments
CALL add_column_if_missing('ai_interview_assessments', 'section_scores', 'JSON NULL AFTER `overall_score`');
CALL add_column_if_missing('ai_interview_assessments', 'recommended_resources', 'JSON NULL AFTER `suggestions`');

UPDATE ai_interview_assessments
SET section_scores = COALESCE(section_scores, JSON_OBJECT());

CALL run_sql_if_column_exists(
    'ai_interview_assessments',
    'technical_score',
    'UPDATE ai_interview_assessments SET section_scores = CASE WHEN section_scores IS NULL OR JSON_LENGTH(section_scores) = 0 THEN JSON_OBJECT(''technical'', COALESCE(technical_score, 0.00), ''communication'', COALESCE(communication_score, 0.00), ''confidence'', COALESCE(confidence_score, 0.00)) ELSE section_scores END'
);

UPDATE ai_interview_assessments
SET recommended_resources = COALESCE(recommended_resources, JSON_ARRAY());

-- 2.8 job_roles
CALL add_column_if_missing('job_roles', 'applicable_experience', 'VARCHAR(100) NULL AFTER `description`');
CALL add_column_if_missing('job_roles', 'typical_tech_stack', 'JSON NULL AFTER `difficulty_level`');
CALL add_column_if_missing('job_roles', 'interview_focus', 'JSON NULL AFTER `typical_tech_stack`');

-- 2.9 interview_templates
CALL add_column_if_missing('interview_templates', 'template_code', 'VARCHAR(50) NULL AFTER `job_role_id`');
CALL add_column_if_missing('interview_templates', 'default_question_count', 'INT NOT NULL DEFAULT 8 AFTER `duration_minutes`');
CALL add_column_if_missing('interview_templates', 'question_type_distribution', 'JSON NULL AFTER `default_question_count`');
CALL add_column_if_missing('interview_templates', 'difficulty_distribution', 'JSON NULL AFTER `question_type_distribution`');
CALL add_column_if_missing('interview_templates', 'follow_up_intensity', 'VARCHAR(30) NOT NULL DEFAULT ''MEDIUM'' AFTER `difficulty_distribution`');
CALL add_column_if_missing('interview_templates', 'scoring_weights', 'JSON NULL AFTER `follow_up_intensity`');

UPDATE interview_templates
SET template_code = COALESCE(template_code, CONCAT('template_', id)),
    default_question_count = COALESCE(default_question_count, 8),
    follow_up_intensity = COALESCE(follow_up_intensity, 'MEDIUM');

CALL add_index_if_missing(
    'interview_templates',
    'uk_template_code',
    'ALTER TABLE `interview_templates` ADD UNIQUE INDEX `uk_template_code` (`template_code`)'
);

-- ===========================
-- 3. 新增核心表
-- ===========================

CREATE TABLE IF NOT EXISTS job_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    applicable_experience VARCHAR(100),
    category VARCHAR(50),
    difficulty_level VARCHAR(30) DEFAULT 'ENTRY',
    typical_tech_stack JSON,
    interview_focus JSON,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位主表';

CREATE TABLE IF NOT EXISTS user_job_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_role_id BIGINT NOT NULL,
    priority_level INT DEFAULT 1,
    target_company_type VARCHAR(50),
    target_city VARCHAR(100),
    preparation_status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_job_role (user_id, job_role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户岗位画像表';

CREATE TABLE IF NOT EXISTS job_role_skill_dimensions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_role_id BIGINT NOT NULL,
    dimension_code VARCHAR(50) NOT NULL,
    dimension_name VARCHAR(100) NOT NULL,
    weight DECIMAL(5,2) DEFAULT 0.00,
    description VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_job_role_dimension (job_role_id, dimension_code),
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位能力维度表';

CREATE TABLE IF NOT EXISTS question_job_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    job_role_id BIGINT NOT NULL,
    relevance_score DECIMAL(5,2) DEFAULT 0.00,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_question_job_role (question_id, job_role_id),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目岗位关联表';

CREATE TABLE IF NOT EXISTS question_skill_dimensions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    skill_dimension_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_question_skill_dimension (question_id, skill_dimension_id),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_dimension_id) REFERENCES job_role_skill_dimensions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目能力维度关联表';

CREATE TABLE IF NOT EXISTS knowledge_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_role_id BIGINT,
    title VARCHAR(255) NOT NULL,
    doc_type VARCHAR(50) DEFAULT 'KNOWLEDGE_NOTE',
    topic VARCHAR(100),
    keywords JSON,
    applicable_question_types JSON,
    difficulty_level VARCHAR(20) DEFAULT 'MEDIUM',
    recommended_examples JSON,
    source_type VARCHAR(50) DEFAULT 'MANUAL',
    source_url VARCHAR(500),
    content LONGTEXT,
    summary TEXT,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_job_role_id (job_role_id),
    INDEX idx_status (status),
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识文档表';

CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    job_role_id BIGINT,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    keywords JSON,
    knowledge_points JSON,
    token_count INT DEFAULT 0,
    embedding_status VARCHAR(30) DEFAULT 'PENDING',
    embedding_model VARCHAR(100),
    embedding_vector LONGTEXT,
    embedding_dimension INT,
    embedding_updated_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_document_chunk (document_id, chunk_index),
    FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识片段表';

CREATE TABLE IF NOT EXISTS excellent_answer_examples (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NULL,
    job_role_id BIGINT,
    question_type VARCHAR(50),
    topic VARCHAR(100),
    question_example TEXT,
    title VARCHAR(255),
    answer_content LONGTEXT NOT NULL,
    answer_level VARCHAR(30) DEFAULT 'GOOD',
    structure_template TEXT,
    highlight_points JSON,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优秀回答示例表';

CALL add_column_if_missing('knowledge_documents', 'doc_type', 'VARCHAR(50) DEFAULT ''KNOWLEDGE_NOTE'' AFTER `title`');
CALL add_column_if_missing('knowledge_documents', 'topic', 'VARCHAR(100) NULL AFTER `doc_type`');
CALL add_column_if_missing('knowledge_documents', 'keywords', 'JSON NULL AFTER `topic`');
CALL add_column_if_missing('knowledge_documents', 'applicable_question_types', 'JSON NULL AFTER `keywords`');
CALL add_column_if_missing('knowledge_documents', 'difficulty_level', 'VARCHAR(20) DEFAULT ''MEDIUM'' AFTER `applicable_question_types`');
CALL add_column_if_missing('knowledge_documents', 'recommended_examples', 'JSON NULL AFTER `difficulty_level`');

CALL add_column_if_missing('knowledge_chunks', 'summary', 'TEXT NULL AFTER `content`');
CALL add_column_if_missing('knowledge_chunks', 'knowledge_points', 'JSON NULL AFTER `keywords`');
CALL add_column_if_missing('knowledge_chunks', 'token_count', 'INT DEFAULT 0 AFTER `knowledge_points`');
CALL add_column_if_missing('knowledge_chunks', 'embedding_model', 'VARCHAR(100) NULL AFTER `embedding_status`');
CALL add_column_if_missing('knowledge_chunks', 'embedding_vector', 'LONGTEXT NULL AFTER `embedding_model`');
CALL add_column_if_missing('knowledge_chunks', 'embedding_dimension', 'INT NULL AFTER `embedding_vector`');
CALL add_column_if_missing('knowledge_chunks', 'embedding_updated_at', 'DATETIME NULL AFTER `embedding_dimension`');

CALL add_column_if_missing('excellent_answer_examples', 'question_type', 'VARCHAR(50) NULL AFTER `job_role_id`');
CALL add_column_if_missing('excellent_answer_examples', 'topic', 'VARCHAR(100) NULL AFTER `question_type`');
CALL add_column_if_missing('excellent_answer_examples', 'question_example', 'TEXT NULL AFTER `topic`');
CALL add_column_if_missing('excellent_answer_examples', 'answer_level', 'VARCHAR(30) DEFAULT ''GOOD'' AFTER `answer_content`');
CALL add_column_if_missing('excellent_answer_examples', 'structure_template', 'TEXT NULL AFTER `answer_level`');

CREATE TABLE IF NOT EXISTS interview_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_role_id BIGINT NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    interview_type VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20) DEFAULT 'MEDIUM',
    duration_minutes INT DEFAULT 45,
    default_question_count INT DEFAULT 8,
    question_type_distribution JSON,
    difficulty_distribution JSON,
    follow_up_intensity VARCHAR(30) DEFAULT 'MEDIUM',
    scoring_weights JSON,
    config JSON,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_template_code (template_code),
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试模板表';

CREATE TABLE IF NOT EXISTS interview_template_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT DEFAULT 0,
    is_required BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_template_question (template_id, question_id),
    FOREIGN KEY (template_id) REFERENCES interview_templates(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试模板题目关联表';

CREATE TABLE IF NOT EXISTS interview_follow_up_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    rule_content JSON NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试追问规则表';

CREATE TABLE IF NOT EXISTS interview_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interview_id BIGINT,
    question_id BIGINT,
    job_role_id BIGINT,
    recommendation_type VARCHAR(50) NOT NULL,
    recommendation_reason VARCHAR(500),
    status VARCHAR(30) DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (interview_id) REFERENCES ai_interviews(id) ON DELETE SET NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE SET NULL,
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习推荐表';

CREATE TABLE IF NOT EXISTS user_growth_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_role_id BIGINT,
    snapshot_date DATE NOT NULL,
    overall_score DECIMAL(5,2) DEFAULT 0.00,
    job_match_score DECIMAL(5,2) DEFAULT 0.00,
    dimension_scores JSON,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_job_snapshot (user_id, job_role_id, snapshot_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (job_role_id) REFERENCES job_roles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成长快照表';

-- ===========================
-- 4. 初始化岗位基础数据
-- ===========================

INSERT INTO job_roles (code, name, description, applicable_experience, category, difficulty_level, typical_tech_stack, interview_focus, is_active, created_at, updated_at)
SELECT * FROM (
    SELECT 'java_backend' AS code, 'Java后端开发' AS name, '面向校招和初级岗位的 Java 后端面试岗位' AS description, '校招 / 0-3年' AS applicable_experience, 'backend' AS category, 'ENTRY' AS difficulty_level, JSON_ARRAY('Java', 'Spring Boot', 'MySQL', 'Redis', 'MQ') AS typical_tech_stack, JSON_ARRAY('语言基础', '并发与JVM', '框架能力', '数据库能力', '系统设计', '项目表达') AS interview_focus, TRUE AS is_active, NOW() AS created_at, NOW() AS updated_at
    UNION ALL
    SELECT 'frontend', '前端开发', '面向校招和初级岗位的前端开发面试岗位', '校招 / 0-3年', 'frontend', 'ENTRY', JSON_ARRAY('JavaScript', 'TypeScript', 'Vue 3', 'React', '工程化'), JSON_ARRAY('语言与浏览器基础', '框架原理', '工程化能力', '性能优化', '项目表达'), TRUE, NOW(), NOW()
    UNION ALL
    SELECT 'python', 'Python开发', '面向服务端和数据处理方向的 Python 开发岗位', '校招 / 0-3年', 'backend', 'ENTRY', JSON_ARRAY('Python', 'FastAPI', 'Django', 'MySQL', 'Redis'), JSON_ARRAY('语言特性', 'Web框架', '数据处理', '工程实践', '项目表达'), TRUE, NOW(), NOW()
    UNION ALL
    SELECT 'algorithm', '算法工程师', '偏重算法设计、编码实现与复杂度分析的岗位', '校招 / 1-3年', 'algorithm', 'INTERMEDIATE', JSON_ARRAY('C++', 'Python', '机器学习', '深度学习', 'RAG'), JSON_ARRAY('算法基础', '机器学习', '深度学习', '数据处理', '模型评估', '大模型与RAG', '项目落地'), TRUE, NOW(), NOW()
) AS seed_data
WHERE NOT EXISTS (
    SELECT 1 FROM job_roles jr WHERE jr.code = seed_data.code
);

INSERT IGNORE INTO job_role_skill_dimensions (job_role_id, dimension_code, dimension_name, weight, description, created_at, updated_at)
SELECT jr.id, seed.dimension_code, seed.dimension_name, seed.weight, seed.description, NOW(), NOW()
FROM job_roles jr
JOIN (
    SELECT 'java_backend' AS role_code, 'language' AS dimension_code, '语言基础' AS dimension_name, 20.00 AS weight, 'Java 基础语法、集合、泛型和常见语言特性' AS description
    UNION ALL SELECT 'java_backend', 'jvm_concurrency', '并发与JVM', 20.00, '线程、锁、内存模型、垃圾回收与调优'
    UNION ALL SELECT 'java_backend', 'framework', '框架能力', 18.00, 'Spring、Spring Boot、常见中间件使用与原理'
    UNION ALL SELECT 'java_backend', 'database', '数据库能力', 17.00, 'MySQL、Redis、事务、索引与缓存'
    UNION ALL SELECT 'java_backend', 'system_design', '系统设计', 15.00, '高并发、高可用、微服务与接口设计'
    UNION ALL SELECT 'java_backend', 'project', '项目表达', 10.00, '项目背景、职责、取舍与复盘能力'
    UNION ALL SELECT 'frontend', 'language_browser', '语言与浏览器基础', 22.00, 'JavaScript、TypeScript、浏览器与渲染机制'
    UNION ALL SELECT 'frontend', 'framework', '框架原理', 24.00, 'Vue/React 响应式、组件化与状态管理'
    UNION ALL SELECT 'frontend', 'engineering', '工程化能力', 18.00, '构建工具、模块化、CI/CD 与规范化'
    UNION ALL SELECT 'frontend', 'performance', '性能优化', 18.00, '加载性能、渲染性能与监控定位'
    UNION ALL SELECT 'frontend', 'project', '项目表达', 18.00, '项目设计、交互取舍与问题排查'
    UNION ALL SELECT 'python', 'language', '语言特性', 22.00, 'Python 语法、异步、类型与常用标准库'
    UNION ALL SELECT 'python', 'framework', 'Web框架', 22.00, 'Django / FastAPI 架构与接口设计'
    UNION ALL SELECT 'python', 'data_processing', '数据处理', 18.00, '数据清洗、任务调度与脚本能力'
    UNION ALL SELECT 'python', 'engineering', '工程实践', 18.00, '测试、部署、日志、性能与可维护性'
    UNION ALL SELECT 'python', 'project', '项目表达', 20.00, '项目落地、业务理解与技术取舍'
    UNION ALL SELECT 'algorithm', 'algo_basic', '算法基础', 18.00, '数据结构、复杂度、常见题型'
    UNION ALL SELECT 'algorithm', 'ml', '机器学习', 14.00, '监督学习、损失函数、特征工程'
    UNION ALL SELECT 'algorithm', 'dl', '深度学习', 14.00, '网络结构、训练流程、调参与推理'
    UNION ALL SELECT 'algorithm', 'data_processing', '数据处理', 12.00, '数据清洗、特征构建与数据管线'
    UNION ALL SELECT 'algorithm', 'evaluation', '模型评估', 12.00, '指标体系、离线评估与线上验证'
    UNION ALL SELECT 'algorithm', 'llm_rag', '大模型与RAG', 15.00, '向量检索、Prompt、评测与应用落地'
    UNION ALL SELECT 'algorithm', 'project', '项目落地', 15.00, '业务场景、实验设计与结果复盘'
) seed ON seed.role_code = jr.code;

INSERT IGNORE INTO interview_templates (job_role_id, template_code, name, interview_type, difficulty, duration_minutes, default_question_count, question_type_distribution, difficulty_distribution, follow_up_intensity, scoring_weights, config, created_at, updated_at)
SELECT jr.id,
       CONCAT(jr.code, '_default'),
       CONCAT(jr.name, '默认模板'),
       CASE WHEN jr.code = 'algorithm' THEN 'algorithm' ELSE 'technical' END,
       CASE WHEN jr.code = 'algorithm' THEN 'HARD' ELSE 'MEDIUM' END,
       CASE WHEN jr.code = 'algorithm' THEN 60 ELSE 45 END,
       CASE WHEN jr.code = 'algorithm' THEN 10 ELSE 8 END,
       CASE
           WHEN jr.code = 'java_backend' THEN JSON_OBJECT('concept', 35, 'project', 25, 'system_design', 20, 'behavioral', 20)
           WHEN jr.code = 'frontend' THEN JSON_OBJECT('concept', 30, 'project', 30, 'system_design', 15, 'behavioral', 25)
           WHEN jr.code = 'python' THEN JSON_OBJECT('concept', 35, 'project', 30, 'system_design', 15, 'behavioral', 20)
           ELSE JSON_OBJECT('algorithm', 45, 'concept', 20, 'project', 20, 'behavioral', 15)
       END,
       CASE
           WHEN jr.code = 'algorithm' THEN JSON_OBJECT('EASY', 20, 'MEDIUM', 45, 'HARD', 35)
           ELSE JSON_OBJECT('EASY', 25, 'MEDIUM', 55, 'HARD', 20)
       END,
       CASE WHEN jr.code = 'algorithm' THEN 'HIGH' ELSE 'MEDIUM' END,
       CASE
           WHEN jr.code = 'java_backend' THEN JSON_OBJECT('technical', 40, 'project', 20, 'communication', 20, 'problem_solving', 20)
           WHEN jr.code = 'frontend' THEN JSON_OBJECT('technical', 35, 'project', 25, 'communication', 20, 'engineering', 20)
           WHEN jr.code = 'python' THEN JSON_OBJECT('technical', 35, 'project', 25, 'communication', 20, 'engineering', 20)
           ELSE JSON_OBJECT('algorithm', 40, 'technical', 20, 'communication', 15, 'problem_solving', 25)
       END,
       JSON_OBJECT('note', CONCAT(jr.name, ' 岗位默认面试配置')),
       NOW(),
       NOW()
FROM job_roles jr
WHERE NOT EXISTS (
    SELECT 1 FROM interview_templates it WHERE it.template_code = CONCAT(jr.code, '_default')
);

-- 为旧用户回填默认目标岗位（如果仅存在一个明确目标可后续人工修正）
UPDATE users u
LEFT JOIN job_roles jr ON jr.code = 'java_backend'
SET u.target_job_role_id = COALESCE(u.target_job_role_id, jr.id)
WHERE u.target_job_role_id IS NULL;

-- ===========================
-- 5. 清理辅助过程
-- ===========================

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS run_sql_if_column_exists;
