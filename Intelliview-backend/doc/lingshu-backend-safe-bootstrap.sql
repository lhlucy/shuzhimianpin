-- 灵枢项目安全建表 / 补表脚本
-- 用途：
-- 1. 在已有数据库上补齐完整核心表结构
-- 2. 保留现有 users 数据，避免影响登录
-- 3. 补齐第 1-8 阶段涉及的核心字段与岗位化数据结构
--
-- 重要说明：
-- 1. 本脚本不会 DROP TABLE，不会删除已有用户数据
-- 2. 执行前仍建议先备份数据库
-- 3. 该脚本适合“老库升级 + 保留账号登录”

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @db = DATABASE();

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

DROP PROCEDURE IF EXISTS modify_column_if_exists;
DELIMITER $$
CREATE PROCEDURE modify_column_if_exists(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_definition TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` MODIFY COLUMN `', p_column_name, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    email_verified BOOLEAN DEFAULT FALSE,
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    phone VARCHAR(20),
    bio VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    github_id VARCHAR(100) UNIQUE,
    github_login VARCHAR(100),
    github_avatar VARCHAR(500),
    github_name VARCHAR(100),
    github_bio VARCHAR(500),
    github_company VARCHAR(100),
    github_blog VARCHAR(200),
    github_location VARCHAR(100),
    last_login_time DATETIME,
    last_login_ip VARCHAR(50),
    login_count INT DEFAULT 0,
    points INT DEFAULT 0,
    level INT DEFAULT 1,
    experience INT DEFAULT 0,
    target_job_role_id BIGINT,
    experience_level VARCHAR(30),
    preferred_company_type VARCHAR(50),
    resume_summary TEXT,
    latest_overall_score DECIMAL(5,2) DEFAULT 0.00,
    latest_job_match_score DECIMAL(5,2) DEFAULT 0.00,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_github_id (github_id),
    INDEX idx_role (role),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon VARCHAR(100),
    parent_id BIGINT,
    sort_order INT DEFAULT 0,
    question_count INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    INDEX idx_slug (slug),
    INDEX idx_parent_id (parent_id),
    INDEX idx_is_visible (is_visible),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    color VARCHAR(20),
    question_count INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    INDEX idx_slug (slug),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    question_text LONGTEXT NOT NULL,
    answer_text LONGTEXT,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category_id BIGINT,
    question_type VARCHAR(50) DEFAULT 'TECHNICAL',
    primary_job_role_id BIGINT,
    skill_dimension_summary VARCHAR(500),
    standard_answer_points JSON,
    common_mistakes JSON,
    follow_up_prompts JSON,
    scoring_points JSON,
    recommended_resources JSON,
    is_for_interview BOOLEAN DEFAULT TRUE,
    is_for_practice BOOLEAN DEFAULT TRUE,
    interview_frequency INT DEFAULT 0,
    source_type VARCHAR(30) DEFAULT 'ADMIN',
    mark_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    browse_count INT DEFAULT 0,
    last_browse_time DATETIME,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    key_points JSON,
    related_questions JSON,
    metadata JSON,
    source VARCHAR(20) DEFAULT 'ADMIN',
    submit_count INT DEFAULT 0,
    accept_count INT DEFAULT 0,
    accept_rate DECIMAL(5,2) DEFAULT 0.00,
    is_visible BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_category_id (category_id),
    INDEX idx_primary_job_role_id (primary_job_role_id),
    INDEX idx_slug (slug),
    INDEX idx_is_visible (is_visible),
    INDEX idx_difficulty (difficulty),
    INDEX idx_question_type (question_type),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question_tags (
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (question_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    content LONGTEXT NOT NULL,
    like_count INT DEFAULT 0,
    view_count INT DEFAULT 0,
    is_approved BOOLEAN DEFAULT FALSE,
    is_anonymous BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id),
    INDEX idx_is_approved (is_approved),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    answer_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT,
    like_count INT DEFAULT 0,
    is_approved BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_answer_id (answer_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS like_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_question (user_id, question_id),
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    keyword VARCHAR(100) NOT NULL,
    search_type VARCHAR(20) DEFAULT 'QUESTION',
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    search_filters VARCHAR(1000),
    created_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100),
    code VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    ip VARCHAR(50),
    user_agent VARCHAR(500),
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expire_time DATETIME,
    create_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    related_id BIGINT,
    related_type VARCHAR(20),
    created_at DATETIME NOT NULL,
    read_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS login_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    login_type VARCHAR(20) NOT NULL,
    login_ip VARCHAR(50),
    login_location VARCHAR(100),
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    fail_reason VARCHAR(200),
    login_time DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    UNIQUE KEY uk_user_job_role (user_id, job_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_role_skill_dimensions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_role_id BIGINT NOT NULL,
    dimension_code VARCHAR(50) NOT NULL,
    dimension_name VARCHAR(100) NOT NULL,
    weight DECIMAL(5,2) DEFAULT 0.00,
    description VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_job_role_dimension (job_role_id, dimension_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question_job_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    job_role_id BIGINT NOT NULL,
    relevance_score DECIMAL(5,2) DEFAULT 0.00,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_question_job_role (question_id, job_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question_skill_dimensions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    skill_dimension_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_question_skill_dimension (question_id, skill_dimension_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    INDEX idx_job_role_id (job_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    UNIQUE KEY uk_document_chunk (document_id, chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    UNIQUE KEY uk_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_template_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT DEFAULT 0,
    is_required BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_template_question (template_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_follow_up_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    rule_content JSON NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_growth_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_role_id BIGINT,
    snapshot_date DATE NOT NULL,
    overall_score DECIMAL(5,2) DEFAULT 0.00,
    job_match_score DECIMAL(5,2) DEFAULT 0.00,
    dimension_scores JSON,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_job_snapshot (user_id, job_role_id, snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    job_role_id BIGINT,
    target_position VARCHAR(100),
    interview_type VARCHAR(50) NOT NULL DEFAULT 'technical',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    duration INT DEFAULT 0,
    actual_duration INT DEFAULT 0,
    total_score DECIMAL(5,2) DEFAULT 0.00,
    difficulty VARCHAR(20) DEFAULT 'MEDIUM',
    skill_tags JSON,
    experience_level VARCHAR(30),
    company_type VARCHAR(50),
    voice_enabled BOOLEAN DEFAULT FALSE,
    project_summary TEXT,
    template_code VARCHAR(50),
    assessment_focus JSON,
    config_snapshot JSON,
    question_count INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    started_at DATETIME,
    ended_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_job_role_id (job_role_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_interview_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20) DEFAULT 'MEDIUM',
    topic VARCHAR(100),
    estimated_time INT DEFAULT 120,
    question_order INT DEFAULT 0,
    context JSON,
    source_question_id BIGINT,
    parent_question_id BIGINT,
    created_at DATETIME NOT NULL,
    INDEX idx_interview_id (interview_id),
    INDEX idx_source_question_id (source_question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_interview_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    audio_url VARCHAR(500),
    transcript_text TEXT,
    input_mode VARCHAR(20) DEFAULT 'TEXT',
    duration INT DEFAULT 0,
    confidence_level INT DEFAULT 0,
    asr_confidence DECIMAL(5,2),
    speaking_rate DECIMAL(8,2),
    pause_count INT DEFAULT 0,
    clarity_score DECIMAL(5,2),
    expression_confidence DECIMAL(5,2),
    emotion_label VARCHAR(50),
    score DECIMAL(5,2) DEFAULT 0.00,
    feedback JSON,
    keyword_analysis JSON,
    expression_analysis JSON,
    submitted_at DATETIME NOT NULL,
    INDEX idx_interview_id (interview_id),
    INDEX idx_question_id (question_id),
    INDEX idx_submitted_at (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_interview_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    overall_score DECIMAL(5,2) DEFAULT 0.00,
    section_scores JSON,
    strengths JSON,
    weaknesses JSON,
    suggestions JSON,
    recommended_resources JSON,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_interview_id (interview_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_interview_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interview_id BIGINT NOT NULL,
    interview_type VARCHAR(50),
    score DECIMAL(5,2) DEFAULT 0.00,
    duration INT DEFAULT 0,
    interview_date DATETIME,
    position VARCHAR(100),
    skill_tags JSON,
    strengths JSON,
    weaknesses JSON,
    created_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_interview_id (interview_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_practice_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    view_answer BOOLEAN DEFAULT FALSE,
    duration INT DEFAULT 0,
    job_role_id BIGINT,
    question_type VARCHAR(50),
    self_score DECIMAL(5,2),
    system_score DECIMAL(5,2),
    weakness_tags JSON,
    practice_source VARCHAR(50) DEFAULT 'DIRECT',
    linked_interview_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id),
    INDEX idx_job_role_id (job_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CALL add_column_if_missing('users', 'target_job_role_id', 'BIGINT NULL AFTER `experience`');
CALL add_column_if_missing('users', 'experience_level', 'VARCHAR(30) NULL AFTER `target_job_role_id`');
CALL add_column_if_missing('users', 'preferred_company_type', 'VARCHAR(50) NULL AFTER `experience_level`');
CALL add_column_if_missing('users', 'resume_summary', 'TEXT NULL AFTER `preferred_company_type`');
CALL add_column_if_missing('users', 'latest_overall_score', 'DECIMAL(5,2) NOT NULL DEFAULT 0.00 AFTER `resume_summary`');
CALL add_column_if_missing('users', 'latest_job_match_score', 'DECIMAL(5,2) NOT NULL DEFAULT 0.00 AFTER `latest_overall_score`');

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

CALL add_column_if_missing('ai_interview_questions', 'content', 'TEXT NULL AFTER `interview_id`');
CALL add_column_if_missing('ai_interview_questions', 'type', 'VARCHAR(50) NULL AFTER `content`');
CALL add_column_if_missing('ai_interview_questions', 'topic', 'VARCHAR(100) NULL AFTER `difficulty`');
CALL add_column_if_missing('ai_interview_questions', 'estimated_time', 'INT NOT NULL DEFAULT 120 AFTER `topic`');
CALL add_column_if_missing('ai_interview_questions', 'question_order', 'INT NOT NULL DEFAULT 0 AFTER `estimated_time`');
CALL add_column_if_missing('ai_interview_questions', 'context', 'JSON NULL AFTER `question_order`');
CALL add_column_if_missing('ai_interview_questions', 'source_question_id', 'BIGINT NULL AFTER `context`');
CALL add_column_if_missing('ai_interview_questions', 'parent_question_id', 'BIGINT NULL AFTER `source_question_id`');
CALL modify_column_if_exists('ai_interview_questions', 'question_text', 'TEXT NULL');
CALL modify_column_if_exists('ai_interview_questions', 'question_type', 'VARCHAR(50) NULL');
CALL modify_column_if_exists('ai_interview_questions', 'sort_order', 'INT NULL');
CALL modify_column_if_exists('ai_interview_questions', 'time_limit', 'INT NULL');

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

CALL add_column_if_missing('ai_interview_assessments', 'section_scores', 'JSON NULL AFTER `overall_score`');
CALL add_column_if_missing('ai_interview_assessments', 'recommended_resources', 'JSON NULL AFTER `suggestions`');

CALL add_column_if_missing('user_practice_history', 'job_role_id', 'BIGINT NULL AFTER `question_id`');
CALL add_column_if_missing('user_practice_history', 'question_type', 'VARCHAR(50) NULL AFTER `job_role_id`');
CALL add_column_if_missing('user_practice_history', 'self_score', 'DECIMAL(5,2) NULL AFTER `duration`');
CALL add_column_if_missing('user_practice_history', 'system_score', 'DECIMAL(5,2) NULL AFTER `self_score`');
CALL add_column_if_missing('user_practice_history', 'weakness_tags', 'JSON NULL AFTER `system_score`');
CALL add_column_if_missing('user_practice_history', 'practice_source', 'VARCHAR(50) NOT NULL DEFAULT ''DIRECT'' AFTER `weakness_tags`');
CALL add_column_if_missing('user_practice_history', 'linked_interview_id', 'BIGINT NULL AFTER `practice_source`');

CALL add_index_if_missing('questions', 'idx_primary_job_role_id', 'ALTER TABLE `questions` ADD INDEX `idx_primary_job_role_id` (`primary_job_role_id`)');
CALL add_index_if_missing('questions', 'idx_question_type', 'ALTER TABLE `questions` ADD INDEX `idx_question_type` (`question_type`)');
CALL add_index_if_missing('ai_interviews', 'idx_target_position', 'ALTER TABLE `ai_interviews` ADD INDEX `idx_target_position` (`target_position`)');
CALL add_index_if_missing('ai_interview_questions', 'idx_source_question_id', 'ALTER TABLE `ai_interview_questions` ADD INDEX `idx_source_question_id` (`source_question_id`)');
CALL add_index_if_missing('ai_interview_answers', 'idx_submitted_at', 'ALTER TABLE `ai_interview_answers` ADD INDEX `idx_submitted_at` (`submitted_at`)');
CALL add_index_if_missing('user_practice_history', 'idx_job_role_id', 'ALTER TABLE `user_practice_history` ADD INDEX `idx_job_role_id` (`job_role_id`)');

UPDATE users
SET experience_level = COALESCE(experience_level, 'CAMPUS'),
    latest_overall_score = COALESCE(latest_overall_score, 0.00),
    latest_job_match_score = COALESCE(latest_job_match_score, 0.00);

UPDATE questions
SET question_type = COALESCE(question_type, 'TECHNICAL'),
    is_for_interview = COALESCE(is_for_interview, TRUE),
    is_for_practice = COALESCE(is_for_practice, TRUE),
    interview_frequency = COALESCE(interview_frequency, 0),
    source_type = COALESCE(source_type, 'ADMIN');

UPDATE ai_interviews
SET actual_duration = COALESCE(actual_duration, duration, 0),
    total_score = COALESCE(total_score, 0.00),
    difficulty = COALESCE(difficulty, 'MEDIUM'),
    question_count = COALESCE(question_count, 0),
    voice_enabled = COALESCE(voice_enabled, FALSE);

UPDATE ai_interview_answers
SET confidence_level = COALESCE(confidence_level, 0),
    score = COALESCE(score, 0.00),
    submitted_at = COALESCE(submitted_at, NOW());

UPDATE ai_interview_questions
SET question_text = COALESCE(question_text, content),
    question_type = COALESCE(question_type, type),
    sort_order = COALESCE(sort_order, question_order),
    time_limit = COALESCE(time_limit, estimated_time, 120);

INSERT INTO job_roles (code, name, description, applicable_experience, category, difficulty_level, typical_tech_stack, interview_focus, is_active, created_at, updated_at)
SELECT * FROM (
    SELECT 'java_backend' AS code, 'Java后端开发' AS name, '面向校招和初级岗位的 Java 后端面试岗位' AS description, '校招 / 0-3年' AS applicable_experience, 'backend' AS category, 'ENTRY' AS difficulty_level, JSON_ARRAY('Java', 'Spring Boot', 'MySQL', 'Redis', 'MQ') AS typical_tech_stack, JSON_ARRAY('语言基础', '并发与JVM', '框架能力', '数据库能力', '系统设计', '项目表达') AS interview_focus, TRUE AS is_active, NOW() AS created_at, NOW() AS updated_at
    UNION ALL
    SELECT 'frontend', '前端开发', '面向校招和初级岗位的前端开发面试岗位', '校招 / 0-3年', 'frontend', 'ENTRY', JSON_ARRAY('JavaScript', 'TypeScript', 'Vue 3', 'React', '工程化'), JSON_ARRAY('语言与浏览器基础', '框架原理', '工程化能力', '性能优化', '项目表达'), TRUE, NOW(), NOW()
    UNION ALL
    SELECT 'python', 'Python开发', '面向服务端和数据处理方向的 Python 开发岗位', '校招 / 0-3年', 'backend', 'ENTRY', JSON_ARRAY('Python', 'FastAPI', 'Django', 'MySQL', 'Redis'), JSON_ARRAY('语言特性', 'Web框架', '数据处理', '工程实践', '项目表达'), TRUE, NOW(), NOW()
    UNION ALL
    SELECT 'algorithm', '算法工程师', '偏重算法设计、编码实现与复杂度分析的岗位', '校招 / 1-3年', 'algorithm', 'INTERMEDIATE', JSON_ARRAY('C++', 'Python', '机器学习', '深度学习', 'RAG'), JSON_ARRAY('算法基础', '机器学习', '深度学习', '数据处理', '模型评估', '大模型与RAG', '项目落地'), TRUE, NOW(), NOW()
) seed
WHERE NOT EXISTS (SELECT 1 FROM job_roles jr WHERE jr.code = seed.code);

UPDATE users u
LEFT JOIN job_roles jr ON jr.code = 'java_backend'
SET u.target_job_role_id = COALESCE(u.target_job_role_id, jr.id)
WHERE u.target_job_role_id IS NULL;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS modify_column_if_exists;

SET FOREIGN_KEY_CHECKS = 1;
