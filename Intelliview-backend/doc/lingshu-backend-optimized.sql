-- 优化后的SQL文件：包含表结构创建和必要的初始化数据
-- 优化内容：
-- 1. 改进索引设计，添加必要的外键约束
-- 2. 优化表结构，调整数据类型和默认值
-- 3. 减少冗余数据，保留核心初始化数据
-- 4. 提升SQL语句的可读性和可维护性
-- 5. 优化查询性能，合并重复的更新语句

-- 先删除现有表，确保使用最新的表结构
-- 禁用外键约束检查，避免删除时的外键约束错误
SET FOREIGN_KEY_CHECKS = 0;

-- 按照依赖关系顺序删除：先删除引用其他表的表，再删除被引用的表
DROP TABLE IF EXISTS bank_category_relation;
DROP TABLE IF EXISTS question_banks;
DROP TABLE IF EXISTS papers;
DROP TABLE IF EXISTS user_growth_snapshots;
DROP TABLE IF EXISTS interview_recommendations;
DROP TABLE IF EXISTS interview_follow_up_rules;
DROP TABLE IF EXISTS interview_template_questions;
DROP TABLE IF EXISTS interview_templates;
DROP TABLE IF EXISTS excellent_answer_examples;
DROP TABLE IF EXISTS knowledge_chunks;
DROP TABLE IF EXISTS knowledge_documents;
DROP TABLE IF EXISTS question_skill_dimensions;
DROP TABLE IF EXISTS question_job_roles;
DROP TABLE IF EXISTS user_job_profiles;
DROP TABLE IF EXISTS job_role_skill_dimensions;
DROP TABLE IF EXISTS job_roles;
DROP TABLE IF EXISTS user_interview_history;
DROP TABLE IF EXISTS ai_interview_assessments;
DROP TABLE IF EXISTS ai_interview_answers;
DROP TABLE IF EXISTS ai_interview_questions;
DROP TABLE IF EXISTS ai_interviews;
DROP TABLE IF EXISTS login_records;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS verification_codes;
DROP TABLE IF EXISTS search_history;
DROP TABLE IF EXISTS user_favorites;
DROP TABLE IF EXISTS like_records;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS question_tags;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- 重新启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 创建用户表
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

-- 创建分类表
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

-- 创建标签表
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

-- 创建问题表
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
    INDEX idx_mark_count (mark_count),
    INDEX idx_share_count (share_count),
    INDEX idx_browse_count (browse_count),
    INDEX idx_view_count (view_count),
    INDEX idx_like_count (like_count),
    INDEX idx_created_at (created_at),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建问题标签关联表
CREATE TABLE IF NOT EXISTS question_tags (
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (question_id, tag_id),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建回答表
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
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建评论表
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
    INDEX idx_parent_id (parent_id),
    INDEX idx_is_approved (is_approved),
    INDEX idx_is_deleted (is_deleted),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (answer_id) REFERENCES answers(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建点赞记录表
CREATE TABLE IF NOT EXISTS like_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户收藏表
CREATE TABLE IF NOT EXISTS user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_question (user_id, question_id),
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建搜索历史表
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
    INDEX idx_keyword (keyword),
    INDEX idx_search_type (search_type),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建验证码表
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100),
    code VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    ip VARCHAR(50),
    user_agent VARCHAR(500),
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expire_time DATETIME,
    create_time DATETIME NOT NULL,
    INDEX idx_email_type (email, type),
    INDEX idx_expire_time (expire_time),
    INDEX idx_used (used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建消息表
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
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_type (type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建登录记录表
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
    INDEX idx_login_type (login_type),
    INDEX idx_login_time (login_time),
    INDEX idx_success (success),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建AI面试表
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
    INDEX idx_status (status),
    INDEX idx_interview_type (interview_type),
    INDEX idx_target_position (target_position),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建AI面试问题表
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
    INDEX idx_question_type (type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_source_question_id (source_question_id),
    FOREIGN KEY (interview_id) REFERENCES ai_interviews(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建AI面试回答表
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
    INDEX idx_submitted_at (submitted_at),
    FOREIGN KEY (interview_id) REFERENCES ai_interviews(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES ai_interview_questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建AI面试评估表
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
    UNIQUE KEY uk_interview_id (interview_id),
    INDEX idx_interview_id (interview_id),
    FOREIGN KEY (interview_id) REFERENCES ai_interviews(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户面试历史表
CREATE TABLE IF NOT EXISTS user_interview_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interview_id BIGINT NOT NULL,
    interview_type VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) DEFAULT 0.00,
    duration INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_interview_id (interview_id),
    INDEX idx_interview_type (interview_type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (interview_id) REFERENCES ai_interviews(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户刷题记录表
CREATE TABLE IF NOT EXISTS user_practice_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    job_role_id BIGINT,
    question_type VARCHAR(50),
    completed BOOLEAN DEFAULT false,
    view_answer BOOLEAN DEFAULT false,
    duration INT DEFAULT 0,
    self_score DECIMAL(5,2) DEFAULT NULL,
    system_score DECIMAL(5,2) DEFAULT NULL,
    weakness_tags JSON,
    practice_source VARCHAR(50) DEFAULT 'DIRECT',
    linked_interview_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id),
    INDEX idx_job_role_id (job_role_id),
    INDEX idx_completed (completed),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户刷题记录表';

-- 创建岗位表
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

-- 创建用户岗位画像表
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

-- 创建岗位能力维度表
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

-- 创建题目岗位关联表
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

-- 创建题目能力维度关联表
CREATE TABLE IF NOT EXISTS question_skill_dimensions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    skill_dimension_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_question_skill_dimension (question_id, skill_dimension_id),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_dimension_id) REFERENCES job_role_skill_dimensions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目能力维度关联表';

-- 创建知识文档表
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

-- 创建知识片段表
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

-- 创建优秀回答示例表
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

-- 创建面试模板表
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

-- 创建面试模板题目关联表
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

-- 创建追问规则表
CREATE TABLE IF NOT EXISTS interview_follow_up_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    rule_content JSON NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试追问规则表';

-- 创建练习推荐表
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

-- 创建用户成长快照表
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

-- 创建试卷表
CREATE TABLE IF NOT EXISTS papers (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
  title VARCHAR(255) NOT NULL COMMENT '试卷名称',
  description TEXT COMMENT '试卷描述',
  duration INT(11) NOT NULL DEFAULT 90 COMMENT '考试时长（分钟）',
  question_count INT(11) NOT NULL DEFAULT 0 COMMENT '题目数量',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_status (status),
  KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 创建试卷题目关联表
CREATE TABLE IF NOT EXISTS paper_questions (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  paper_id BIGINT(20) NOT NULL COMMENT '试卷ID',
  question_id BIGINT(20) NOT NULL COMMENT '题目ID',
  sort_order INT(11) NOT NULL DEFAULT 0 COMMENT '排序顺序',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_paper_question (paper_id, question_id),
  KEY idx_paper_id (paper_id),
  KEY idx_question_id (question_id),
  FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE,
  FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- 创建题库表
CREATE TABLE IF NOT EXISTS question_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    icon VARCHAR(50) DEFAULT NULL,
    question_count INT DEFAULT 0,
    popular TINYINT(1) DEFAULT 0,
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_popular (popular),
    INDEX idx_view_count (view_count),
    INDEX idx_title (title(50))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

-- 创建题库分类关联表
CREATE TABLE IF NOT EXISTS bank_category_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bank_category (bank_id, category_id),
    INDEX idx_bank_id (bank_id),
    INDEX idx_category_id (category_id),
    FOREIGN KEY (bank_id) REFERENCES question_banks(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库分类关联表';

-- 插入核心初始化数据

-- 插入用户数据
-- 密码均为 123456 的 BCrypt 哈希值
INSERT INTO users (username, password, email, email_verified, nickname, avatar, role, enabled, create_time, update_time) VALUES
('admin', '$2a$10$z1EiGbsIt5GLbKc1vAmQM.cFJN7T1ho/7m1Iluwuf2PAw0DMQeNVa', 'admin@example.com', true, '管理员', 'https://randomuser.me/api/portraits/men/1.jpg', 'ROLE_ADMIN', true, NOW(), NOW()),
('user', '$2a$10$z1EiGbsIt5GLbKc1vAmQM.cFJN7T1ho/7m1Iluwuf2PAw0DMQeNVa', 'user@example.com', true, '测试用户', 'https://randomuser.me/api/portraits/women/2.jpg', 'ROLE_USER', true, NOW(), NOW()),
('teacher', '$2a$10$z1EiGbsIt5GLbKc1vAmQM.cFJN7T1ho/7m1Iluwuf2PAw0DMQeNVa', 'teacher@example.com', true, '教师用户', 'https://randomuser.me/api/portraits/men/3.jpg', 'ROLE_TEACHER', true, NOW(), NOW()) ON DUPLICATE KEY UPDATE password = VALUES(password), email = VALUES(email), email_verified = VALUES(email_verified), nickname = VALUES(nickname), avatar = VALUES(avatar), role = VALUES(role), enabled = VALUES(enabled), update_time = NOW();

-- 说明：上述密码哈希值对应的明文密码均为 123456
-- 管理员账号：admin / 123456
-- 普通用户账号：user / 123456
-- 教师用户账号：teacher / 123456

-- 插入分类数据
INSERT IGNORE INTO categories (name, slug, description, icon, parent_id, sort_order, question_count, is_visible, created_at) VALUES
('算法', 'algorithm', '算法相关的面试题', 'code', NULL, 1, 0, true, NOW()),
('数据结构', 'data-structure', '数据结构相关的面试题', 'database', NULL, 2, 0, true, NOW()),
('操作系统', 'operating-system', '操作系统相关的面试题', 'server', NULL, 3, 0, true, NOW()),
('计算机网络', 'computer-network', '计算机网络相关的面试题', 'wifi', NULL, 4, 0, true, NOW()),
('数据库', 'database', '数据库相关的面试题', 'database', NULL, 5, 0, true, NOW()),
('前端开发', 'frontend', '前端开发相关的面试题', 'laptop', NULL, 6, 0, true, NOW()),
('后端开发', 'backend', '后端开发相关的面试题', 'server', NULL, 7, 0, true, NOW()),
('移动开发', 'mobile', '移动开发相关的面试题', 'smartphone', NULL, 8, 0, true, NOW());

-- 插入岗位数据
INSERT IGNORE INTO job_roles (code, name, description, applicable_experience, category, difficulty_level, typical_tech_stack, interview_focus, is_active, created_at, updated_at) VALUES
('java_backend', 'Java后端开发', '面向校招和初级岗位的 Java 后端面试岗位', '校招 / 0-3年', 'backend', 'ENTRY', JSON_ARRAY('Java', 'Spring Boot', 'MySQL', 'Redis', 'MQ'), JSON_ARRAY('语言基础', '并发与JVM', '框架能力', '数据库能力', '系统设计', '项目表达'), true, NOW(), NOW()),
('frontend', '前端开发', '面向校招和初级岗位的前端开发面试岗位', '校招 / 0-3年', 'frontend', 'ENTRY', JSON_ARRAY('JavaScript', 'TypeScript', 'Vue 3', 'React', '工程化'), JSON_ARRAY('语言与浏览器基础', '框架原理', '工程化能力', '性能优化', '项目表达'), true, NOW(), NOW()),
('python', 'Python开发', '面向服务端和数据处理方向的 Python 开发岗位', '校招 / 0-3年', 'backend', 'ENTRY', JSON_ARRAY('Python', 'FastAPI', 'Django', 'MySQL', 'Redis'), JSON_ARRAY('语言特性', 'Web框架', '数据处理', '工程实践', '项目表达'), true, NOW(), NOW()),
('algorithm', '算法工程师', '偏重算法设计、编码实现与复杂度分析的岗位', '校招 / 1-3年', 'algorithm', 'INTERMEDIATE', JSON_ARRAY('C++', 'Python', '机器学习', '深度学习', 'RAG'), JSON_ARRAY('算法基础', '机器学习', '深度学习', '数据处理', '模型评估', '大模型与RAG', '项目落地'), true, NOW(), NOW());

-- 插入岗位能力维度
INSERT IGNORE INTO job_role_skill_dimensions (job_role_id, dimension_code, dimension_name, weight, description, created_at, updated_at)
SELECT jr.id, seed.dimension_code, seed.dimension_name, seed.weight, seed.description, NOW(), NOW()
FROM job_roles jr
JOIN (
    SELECT 'java_backend' AS role_code, 'language' AS dimension_code, '语言基础' AS dimension_name, 20.00 AS weight, 'Java 基础语法、集合、泛型和常见语言特性'
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

-- 插入默认面试模板
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
FROM job_roles jr;

-- 插入标签数据
INSERT IGNORE INTO tags (name, slug, description, color, question_count, created_at) VALUES
('Java', 'java', 'Java编程语言', '#007396', 0, NOW()),
('Python', 'python', 'Python编程语言', '#3776AB', 0, NOW()),
('JavaScript', 'javascript', 'JavaScript编程语言', '#F7DF1E', 0, NOW()),
('C++', 'c-plus-plus', 'C++编程语言', '#00599C', 0, NOW()),
('算法', 'algorithm', '算法相关', '#4CAF50', 0, NOW()),
('数据结构', 'data-structure', '数据结构相关', '#2196F3', 0, NOW()),
('数据库', 'database', '数据库相关', '#FF9800', 0, NOW()),
('操作系统', 'operating-system', '操作系统相关', '#9C27B0', 0, NOW()),
('计算机网络', 'computer-network', '计算机网络相关', '#607D8B', 0, NOW()),
('前端', 'frontend', '前端开发相关', '#E91E63', 0, NOW());

-- 插入题库数据
INSERT IGNORE INTO question_banks (title, description, icon, question_count, popular, view_count, created_at, updated_at) VALUES
('Java核心面试题', 'Java核心知识点面试题汇总', 'el-icon-document', 0, 1, 0, NOW(), NOW()),
('Spring Boot面试题', 'Spring Boot框架面试题汇总', 'el-icon-leaf', 0, 1, 0, NOW(), NOW()),
('MySQL面试题', 'MySQL数据库面试题汇总', 'el-icon-s-data', 0, 1, 0, NOW(), NOW()),
('Redis面试题', 'Redis缓存数据库面试题汇总', 'el-icon-s-data', 0, 1, 0, NOW(), NOW()),
('Spring Cloud面试题', 'Spring Cloud微服务框架面试题汇总', 'el-icon-s-marketing', 0, 1, 0, NOW(), NOW()),
('操作系统面试题', '操作系统核心知识点面试题汇总', 'el-icon-cpu', 0, 1, 0, NOW(), NOW()),
('计算机网络面试题', '计算机网络核心知识点面试题汇总', 'el-icon-s-network', 0, 1, 0, NOW(), NOW()),
('前端开发面试题', '前端开发核心知识点面试题汇总', 'el-icon-s-marketing', 0, 1, 0, NOW(), NOW()),
('算法面试题', '算法核心知识点面试题汇总', 'el-icon-s-grid', 0, 1, 0, NOW(), NOW()),
('数据结构面试题', '数据结构核心知识点面试题汇总', 'el-icon-s-order', 0, 1, 0, NOW(), NOW());

-- 插入题库分类关联数据
INSERT IGNORE INTO bank_category_relation (bank_id, category_id) VALUES
(1, 7),
(2, 7),
(3, 5),
(4, 5),
(5, 7),
(6, 3),
(7, 4),
(8, 6),
(9, 1),
(10, 2);

-- 插入基础问题数据
INSERT IGNORE INTO questions (title, slug, description, question_text, answer_text, difficulty, category_id, mark_count, share_count, browse_count, view_count, key_points, related_questions, metadata, source, submit_count, accept_count, accept_rate, is_visible, sort_order, created_by, created_at, updated_at) VALUES
('Java方法重载和方法重写之间的区别是什么?', 'method-overloading-vs-overriding', '详细说明Java中方法重载和方法重写的区别', '## 题目描述\nJava方法重载和方法重写之间的区别是什么?请详细说明两者的概念、用法和核心区别。', '# 回答重点\n\n**方法重载**发生在同一个类中，允许多个同名方法，只要参数列表不同就行，参数个数、类型或顺序不一样都算。主要用于在同一类中定义不同场景下的行为。\n\n**方法重写**发生在继承关系中，子类重写父类的某个方法，参数列表和方法名必须完全相同，从而为该方法提供新的实现。主要用于实现运行时多态。\n\n核心区别：\n\n| 维度       | 重载 Overloading | 重写 Overriding                |\n| ---------- | ---------------- | ------------------------------ |\n| 发生位置   | 同一个类中       | 子类和父类之间                 |\n| 参数列表   | 必须不同         | 必须相同                       |\n| 返回类型   | 可以不同         | 必须相同或是父类返回类型的子类 |\n| 访问修饰符 | 不受限制         | 不能比父类更严格               |\n| 静态方法   | 可以重载         | 不能重写，只能隐藏             |\n| 异常处理   | 不受限制         | 不能抛出比父类更多的异常       |', 'MEDIUM', 7, 10, 5, 100, 80, '[]', '[]', '{}', 'ADMIN', 100, 80, 80.00, true, 1, 1, NOW(), NOW()),
('说说 Java 中 HashMap 的原理？', 'hashmap-principle', 'HashMap是Java中最常用的集合类之一', '## 题目描述\n请详细说明Java中HashMap的实现原理，包括哈希冲突的解决方法、扩容机制等。', '## 解决方案\nHashMap基于哈希表实现，使用数组+链表/红黑树的结构。当链表长度超过8时会转换为红黑树，提高查询效率。\n\n**哈希冲突解决**：使用链地址法，将冲突的元素存储在链表中\n**扩容机制**：当容量超过负载因子（默认0.75）时，容量扩大为原来的2倍\n**红黑树转换**：当链表长度超过8且容量大于64时，链表转换为红黑树', 'MEDIUM', 7, 15, 8, 120, 90, '[]', '[]', '{}', 'ADMIN', 120, 90, 75.00, true, 2, 1, NOW(), NOW()),
('MySQL索引的最左前缀原则是什么？', 'mysql-index-left-prefix', '索引是提高MySQL查询性能的关键', '## 题目描述\n请解释MySQL索引的最左前缀原则，以及如何利用这个原则优化查询。', '## 解决方案\n最左前缀原则是指在使用联合索引时，MySQL会从索引的最左边开始匹配，直到遇到范围查询。\n\n**优化建议**：\n1. 将最常用的查询字段放在联合索引的左侧\n2. 避免在索引列上使用函数或表达式\n3. 合理设计联合索引，覆盖常用查询场景', 'MEDIUM', 5, 18, 10, 150, 120, '[]', '[]', '{}', 'ADMIN', 150, 120, 80.00, true, 3, 1, NOW(), NOW()),
('Java中抽象类和接口的区别是什么？', 'abstract-class-vs-interface', '面向对象编程的核心概念', '## 题目描述\n请详细说明Java中抽象类和接口的区别，以及在什么场景下应该使用抽象类，什么场景下应该使用接口。', '## 解决方案\n**抽象类**：\n- 可以包含抽象方法和具体方法\n- 可以有构造方法\n- 可以包含成员变量\n- 子类只能继承一个抽象类\n- 使用extends关键字继承\n\n**接口**：\n- 只能包含抽象方法（Java 8+可以有默认方法和静态方法）\n- 不能有构造方法\n- 只能包含常量\n- 一个类可以实现多个接口\n- 使用implements关键字实现\n\n**使用场景**：\n- 抽象类：当多个类有共同的行为和状态时使用\n- 接口：当需要定义一组行为规范，而不关心具体实现时使用', 'MEDIUM', 7, 20, 12, 180, 150, '[]', '[]', '{}', 'ADMIN', 180, 150, 83.33, true, 4, 1, NOW(), NOW()),
('Java中的多线程实现方式有哪些？', 'java-multithreading-implementation', '多线程是Java的重要特性', '## 题目描述\n请详细说明Java中实现多线程的几种方式，以及它们之间的区别。', '## 解决方案\nJava中实现多线程的方式主要有：\n\n1. **继承Thread类**：\n   - 重写run()方法\n   - 启动线程调用start()方法\n   - 缺点：Java单继承，不能再继承其他类\n\n2. **实现Runnable接口**：\n   - 实现run()方法\n   - 创建Thread对象，传入Runnable实例\n   - 启动线程调用start()方法\n   - 优点：可以同时实现其他接口\n\n3. **实现Callable接口**：\n   - 实现call()方法，有返回值\n   - 需要配合Future和ExecutorService使用\n   - 优点：可以获取线程执行结果\n\n4. **使用线程池**：\n   - 通过ExecutorService创建线程池\n   - 提交任务到线程池执行\n   - 优点：提高线程复用率，管理线程生命周期', 'MEDIUM', 7, 25, 15, 200, 160, '[]', '[]', '{}', 'ADMIN', 200, 160, 80.00, true, 5, 1, NOW(), NOW()),
('JVM的内存结构是怎样的？', 'jvm-memory-structure', '理解JVM内存结构对性能优化至关重要', '## 题目描述\n请详细说明JVM的内存结构，包括各个区域的作用和特点。', '## 解决方案\nJVM内存结构主要包括：\n\n1. **程序计数器**：\n   - 线程私有\n   - 存储当前线程执行的字节码行号\n   - 唯一不会发生OutOfMemoryError的区域\n\n2. **虚拟机栈**：\n   - 线程私有\n   - 存储局部变量表、操作数栈、方法出口等\n   - 可能发生StackOverflowError和OutOfMemoryError\n\n3. **本地方法栈**：\n   - 线程私有\n   - 为Native方法服务\n   - 可能发生StackOverflowError和OutOfMemoryError\n\n4. **堆**：\n   - 线程共享\n   - 存储对象实例\n   - 垃圾回收的主要区域\n   - 可能发生OutOfMemoryError\n\n5. **方法区**：\n   - 线程共享\n   - 存储类信息、常量、静态变量等\n   - JDK 8+使用元空间实现\n   - 可能发生OutOfMemoryError', 'HARD', 7, 30, 20, 250, 200, '[]', '[]', '{}', 'ADMIN', 250, 200, 80.00, true, 6, 1, NOW(), NOW()),
('Spring Boot的自动配置原理是什么？', 'spring-boot-auto-configuration', 'Spring Boot的核心特性', '## 题目描述\n请详细说明Spring Boot的自动配置原理，以及它是如何实现的。', '## 解决方案\nSpring Boot的自动配置原理基于以下核心机制：\n\n1. **@EnableAutoConfiguration注解**：\n   - 启用自动配置功能\n   - 导入AutoConfigurationImportSelector类\n\n2. **SpringFactoriesLoader**：\n   - 加载META-INF/spring.factories文件\n   - 读取自动配置类的全限定名\n\n3. **条件注解**：\n   - @ConditionalOnClass：当类路径存在指定类时生效\n   - @ConditionalOnBean：当容器存在指定Bean时生效\n   - @ConditionalOnProperty：当配置属性满足条件时生效\n   - 等等\n\n4. **配置元数据**：\n   - 生成配置提示信息\n   - 支持IDE的配置补全\n\n自动配置的流程：\n1. 应用启动时，@EnableAutoConfiguration生效\n2. AutoConfigurationImportSelector加载候选配置类\n3. 根据条件注解过滤配置类\n4. 将符合条件的配置类注册到容器中', 'MEDIUM', 7, 35, 25, 300, 240, '[]', '[]', '{}', 'ADMIN', 300, 240, 80.00, true, 7, 1, NOW(), NOW()),
('什么是RESTful API？如何设计一个好的RESTful API？', 'restful-api-design', 'RESTful API是现代Web服务的标准', '## 题目描述\n请详细说明什么是RESTful API，以及如何设计一个好的RESTful API。', '## 解决方案\n**RESTful API**是基于REST（Representational State Transfer）架构风格的API设计规范。\n\n**设计原则**：\n\n1. **资源标识**：\n   - 使用URI标识资源\n   - 资源用名词表示，不用动词\n   - 例如：/users，/products\n\n2. **HTTP方法**：\n   - GET：获取资源\n   - POST：创建资源\n   - PUT：更新资源\n   - DELETE：删除资源\n   - PATCH：部分更新资源\n\n3. **状态码**：\n   - 200 OK：成功\n   - 201 Created：创建成功\n   - 204 No Content：无内容\n   - 400 Bad Request：请求错误\n   - 401 Unauthorized：未授权\n   - 403 Forbidden：禁止访问\n   - 404 Not Found：资源不存在\n   - 500 Internal Server Error：服务器错误\n\n4. **无状态**：\n   - 服务器不存储客户端状态\n   - 客户端每次请求都携带必要信息\n\n5. **缓存**：\n   - 支持HTTP缓存机制\n   - 减少服务器负载\n\n6. **版本控制**：\n   - 在URI中包含版本信息\n   - 例如：/v1/users', 'MEDIUM', 7, 15, 10, 150, 120, '[]', '[]', '{}', 'ADMIN', 150, 120, 80.00, true, 8, 1, NOW(), NOW()),
('计算机网络OSI七层模型和TCP/IP四层模型的区别是什么？', 'osi-vs-tcpip-model', '计算机网络的基础概念', '## 题目描述\n请详细说明OSI七层模型和TCP/IP四层模型的区别，以及各层的主要功能。', '## 解决方案\n**OSI七层模型**：\n\n1. **物理层**：\n   - 传输比特流\n   - 定义物理设备规范\n\n2. **数据链路层**：\n   - 传输帧\n   - 错误检测与纠正\n\n3. **网络层**：\n   - 传输数据包\n   - 路由选择\n   - IP地址\n\n4. **传输层**：\n   - 传输段\n   - 端到端通信\n   - TCP/UDP\n\n5. **会话层**：\n   - 建立、维护、终止会话\n\n6. **表示层**：\n   - 数据格式转换\n   - 加密解密\n\n7. **应用层**：\n   - 应用程序接口\n   - HTTP、FTP、SMTP等\n\n**TCP/IP四层模型**：\n\n1. **网络接口层**：\n   - 对应OSI物理层和数据链路层\n\n2. **网络层**：\n   - 对应OSI网络层\n   - IP协议\n\n3. **传输层**：\n   - 对应OSI传输层\n   - TCP/UDP协议\n\n4. **应用层**：\n   - 对应OSI会话层、表示层、应用层\n   - HTTP、FTP、SMTP等\n\n**区别**：\n- OSI是理论模型，TCP/IP是实际应用的协议栈\n- OSI层次更多，TCP/IP更简洁\n- TCP/IP在实践中被广泛使用', 'MEDIUM', 4, 20, 12, 180, 140, '[]', '[]', '{}', 'ADMIN', 180, 140, 77.78, true, 9, 1, NOW(), NOW()),
('操作系统中的死锁是什么？如何避免死锁？', 'os-deadlock', '操作系统的重要概念', '## 题目描述\n请详细说明什么是死锁，死锁产生的必要条件，以及如何避免死锁。', '## 解决方案\n**死锁**是指两个或多个进程在执行过程中，因争夺资源而造成的一种互相等待的现象。\n\n**死锁产生的必要条件**：\n\n1. **互斥条件**：\n   - 资源不能被多个进程同时使用\n\n2. **请求与保持条件**：\n   - 进程已获得的资源在使用完之前不能被剥夺\n\n3. **不剥夺条件**：\n   - 进程获得的资源在未使用完之前，不能被其他进程强行剥夺\n\n4. **循环等待条件**：\n   - 若干进程之间形成头尾相接的循环等待资源关系\n\n**避免死锁的方法**：\n\n1. **破坏互斥条件**：\n   - 允许资源共享使用\n\n2. **破坏请求与保持条件**：\n   - 进程在开始执行前申请全部所需资源\n\n3. **破坏不剥夺条件**：\n   - 允许进程在需要时剥夺其他进程的资源\n\n4. **破坏循环等待条件**：\n   - 对资源进行编号，进程按顺序申请资源\n\n**死锁的处理策略**：\n- 预防死锁：破坏死锁的必要条件\n- 避免死锁：使用银行家算法等安全状态检查\n- 检测死锁：定期检查系统是否存在死锁\n- 解除死锁：终止某些进程或剥夺资源', 'MEDIUM', 3, 18, 10, 160, 120, '[]', '[]', '{}', 'ADMIN', 160, 120, 75.00, true, 10, 1, NOW(), NOW()),
('什么是算法的时间复杂度和空间复杂度？', 'algorithm-complexity', '算法分析的基础概念', '## 题目描述\n请详细说明什么是算法的时间复杂度和空间复杂度，以及如何分析它们。', '## 解决方案\n**时间复杂度**是指算法执行所需的时间与问题规模之间的关系。\n\n**空间复杂度**是指算法执行所需的存储空间与问题规模之间的关系。\n\n**常见的时间复杂度**（从低到高）：\n- O(1)：常数时间复杂度\n- O(log n)：对数时间复杂度\n- O(n)：线性时间复杂度\n- O(n log n)：线性对数时间复杂度\n- O(n²)：平方时间复杂度\n- O(n³)：立方时间复杂度\n- O(2ⁿ)：指数时间复杂度\n- O(n!)：阶乘时间复杂度\n\n**分析方法**：\n1. **时间复杂度**：\n   - 找出执行次数最多的语句\n   - 计算语句执行次数的数量级\n   - 忽略常数项和低次项\n\n2. **空间复杂度**：\n   - 计算算法所需的额外存储空间\n   - 包括局部变量、数据结构、递归调用栈等\n   - 忽略常数项\n\n**示例**：\n- 顺序查找：O(n)\n- 二分查找：O(log n)\n- 冒泡排序：O(n²)\n- 快速排序：平均O(n log n)，最坏O(n²)\n- 归并排序：O(n log n)', 'MEDIUM', 1, 22, 14, 190, 150, '[]', '[]', '{}', 'ADMIN', 190, 150, 78.95, true, 11, 1, NOW(), NOW()),
('前端中的闭包是什么？有什么作用？', 'frontend-closure', 'JavaScript的重要概念', '## 题目描述\n请详细说明什么是闭包，闭包的作用，以及闭包可能导致的问题。', '## 解决方案\n**闭包**是指有权访问另一个函数作用域中变量的函数。\n\n**闭包的形成条件**：\n1. 函数嵌套\n2. 内部函数引用外部函数的变量\n3. 内部函数在外部可访问\n\n**闭包的作用**：\n1. **保存变量**：\n   - 延长变量的生命周期\n   - 实现私有变量\n\n2. **模块化**：\n   - 实现封装\n   - 避免全局变量污染\n\n3. **函数工厂**：\n   - 根据不同参数创建不同功能的函数\n\n**闭包可能导致的问题**：\n1. **内存泄漏**：\n   - 闭包会持有外部函数的变量，导致这些变量无法被垃圾回收\n\n2. **性能问题**：\n   - 闭包的创建和执行会比普通函数慢\n\n**使用建议**：\n- 合理使用闭包，避免不必要的闭包\n- 在不需要使用闭包时，及时释放引用\n- 注意闭包中的this指向问题', 'MEDIUM', 6, 16, 8, 140, 110, '[]', '[]', '{}', 'ADMIN', 140, 110, 78.57, true, 12, 1, NOW(), NOW()),
('什么是数据结构中的栈和队列？它们有什么区别？', 'data-structure-stack-queue', '数据结构的基础概念', '## 题目描述\n请详细说明什么是栈和队列，它们的特点和区别，以及常见的应用场景。', '## 解决方案\n**栈（Stack）**是一种后进先出（LIFO, Last In First Out）的数据结构。\n\n**队列（Queue）**是一种先进先出（FIFO, First In First Out）的数据结构。\n\n**栈的特点**：\n- 只能在一端（栈顶）进行插入和删除操作\n- 主要操作：push（入栈）、pop（出栈）、peek（查看栈顶元素）\n- 时间复杂度：O(1)\n\n**队列的特点**：\n- 只能在一端（队尾）插入，在另一端（队头）删除\n- 主要操作：enqueue（入队）、dequeue（出队）、peek（查看队头元素）\n- 时间复杂度：O(1)\n\n**常见应用场景**：\n\n**栈**：\n- 函数调用栈\n- 表达式求值\n- 括号匹配\n- 浏览器的前进/后退功能\n\n**队列**：\n- 任务调度\n- 消息队列\n- 广度优先搜索（BFS）\n- 缓冲区处理\n\n**实现方式**：\n- 栈：数组或链表\n- 队列：数组或链表\n\n**特殊队列**：\n- 双端队列（Deque）：两端都可以插入和删除\n- 优先队列：按照优先级出队', 'MEDIUM', 2, 20, 12, 170, 130, '[]', '[]', '{}', 'ADMIN', 170, 130, 76.47, true, 13, 1, NOW(), NOW());

-- 插入标签关联数据
INSERT IGNORE INTO question_tags (question_id, tag_id) VALUES
((SELECT id FROM questions WHERE slug = 'method-overloading-vs-overriding'), 1),
((SELECT id FROM questions WHERE slug = 'hashmap-principle'), 1),
((SELECT id FROM questions WHERE slug = 'mysql-index-left-prefix'), 7),
((SELECT id FROM questions WHERE slug = 'abstract-class-vs-interface'), 1),
((SELECT id FROM questions WHERE slug = 'java-multithreading-implementation'), 1),
((SELECT id FROM questions WHERE slug = 'jvm-memory-structure'), 1),
((SELECT id FROM questions WHERE slug = 'spring-boot-auto-configuration'), 1),
((SELECT id FROM questions WHERE slug = 'restful-api-design'), 1),
((SELECT id FROM questions WHERE slug = 'osi-vs-tcpip-model'), 9),
((SELECT id FROM questions WHERE slug = 'os-deadlock'), 8),
((SELECT id FROM questions WHERE slug = 'algorithm-complexity'), 5),
((SELECT id FROM questions WHERE slug = 'frontend-closure'), 10),
((SELECT id FROM questions WHERE slug = 'data-structure-stack-queue'), 6);

-- 插入试卷数据
INSERT IGNORE INTO papers (title, description, duration, question_count, status) VALUES
('前端开发工程师笔试', '包含 HTML、CSS、JavaScript、Vue 等前端相关知识点', 90, 20, 1),
('后端开发工程师笔试', '包含 Java、Python、Node.js、数据库等后端相关知识点', 120, 25, 1),
('算法工程师笔试', '包含数据结构、算法设计、复杂度分析等相关知识点', 100, 15, 0),
('全栈开发工程师笔试', '包含前端、后端、数据库、DevOps 等全栈相关知识点', 150, 30, 1),
('JavaScript 专项测试', '专注于 JavaScript 语言特性、ES6+、异步编程等知识点', 80, 18, 1),
('Vue 3 专项测试', '专注于 Vue 3 核心概念、Composition API、响应式原理等知识点', 70, 16, 1),
('React 专项测试', '专注于 React 核心概念、Hooks、状态管理等知识点', 70, 16, 0),
('数据库专项测试', '包含 SQL、NoSQL、数据库设计、优化等知识点', 90, 22, 1),
('网络安全专项测试', '包含 Web 安全、加密算法、漏洞防护等知识点', 80, 14, 1),
('DevOps 专项测试', '包含 CI/CD、容器化、自动化部署等知识点', 100, 19, 1);

-- 插入试卷题目关联数据
INSERT IGNORE INTO paper_questions (paper_id, question_id, sort_order) VALUES
-- 前端开发工程师笔试
(1, (SELECT id FROM questions WHERE slug = 'frontend-closure'), 1),
(1, (SELECT id FROM questions WHERE slug = 'restful-api-design'), 2),

-- 后端开发工程师笔试
(2, (SELECT id FROM questions WHERE slug = 'method-overloading-vs-overriding'), 1),
(2, (SELECT id FROM questions WHERE slug = 'hashmap-principle'), 2),
(2, (SELECT id FROM questions WHERE slug = 'abstract-class-vs-interface'), 3),
(2, (SELECT id FROM questions WHERE slug = 'java-multithreading-implementation'), 4),
(2, (SELECT id FROM questions WHERE slug = 'jvm-memory-structure'), 5),
(2, (SELECT id FROM questions WHERE slug = 'spring-boot-auto-configuration'), 6),

-- 算法工程师笔试
(3, (SELECT id FROM questions WHERE slug = 'algorithm-complexity'), 1),
(3, (SELECT id FROM questions WHERE slug = 'data-structure-stack-queue'), 2),

-- 全栈开发工程师笔试
(4, (SELECT id FROM questions WHERE slug = 'method-overloading-vs-overriding'), 1),
(4, (SELECT id FROM questions WHERE slug = 'frontend-closure'), 2),
(4, (SELECT id FROM questions WHERE slug = 'mysql-index-left-prefix'), 3),

-- JavaScript 专项测试
(5, (SELECT id FROM questions WHERE slug = 'frontend-closure'), 1),
(5, (SELECT id FROM questions WHERE slug = 'restful-api-design'), 2),

-- 数据库专项测试
(8, (SELECT id FROM questions WHERE slug = 'mysql-index-left-prefix'), 1);

-- 插入答案数据
INSERT IGNORE INTO answers (user_id, question_id, content, like_count, view_count, is_approved, is_anonymous, is_deleted, created_at, updated_at) VALUES
(1, (SELECT id FROM questions WHERE slug = 'method-overloading-vs-overriding'), '# 回答重点\n\n**方法重载**发生在同一个类中，允许多个同名方法，只要参数列表不同就行，参数个数、类型或顺序不一样都算。主要用于在同一类中定义不同场景下的行为。\n\n**方法重写**发生在继承关系中，子类重写父类的某个方法，参数列表和方法名必须完全相同，从而为该方法提供新的实现。主要用于实现运行时多态。\n\n核心区别：\n\n| 维度       | 重载 Overloading | 重写 Overriding                |\n| ---------- | ---------------- | ------------------------------ |\n| 发生位置   | 同一个类中       | 子类和父类之间                 |\n| 参数列表   | 必须不同         | 必须相同                       |\n| 返回类型   | 可以不同         | 必须相同或是父类返回类型的子类 |\n| 访问修饰符 | 不受限制         | 不能比父类更严格               |\n| 静态方法   | 可以重载         | 不能重写，只能隐藏             |\n| 异常处理   | 不受限制         | 不能抛出比父类更多的异常       |', 50, 1000, true, false, false, NOW(), NOW()),
(1, (SELECT id FROM questions WHERE slug = 'hashmap-principle'), '## 解决方案\nHashMap基于哈希表实现，使用数组+链表/红黑树的结构。当链表长度超过8时会转换为红黑树，提高查询效率。\n\n**哈希冲突解决**：使用链地址法，将冲突的元素存储在链表中\n**扩容机制**：当容量超过负载因子（默认0.75）时，容量扩大为原来的2倍\n**红黑树转换**：当链表长度超过8且容量大于64时，链表转换为红黑树', 45, 900, true, false, false, NOW(), NOW()),
(1, (SELECT id FROM questions WHERE slug = 'mysql-index-left-prefix'), '## 解决方案\n最左前缀原则是指在使用联合索引时，MySQL会从索引的最左边开始匹配，直到遇到范围查询。\n\n**优化建议**：\n1. 将最常用的查询字段放在联合索引的左侧\n2. 避免在索引列上使用函数或表达式\n3. 合理设计联合索引，覆盖常用查询场景', 40, 800, true, false, false, NOW(), NOW());

-- 统一更新计数
-- 更新分类的问题计数
UPDATE categories SET question_count = (SELECT COUNT(*) FROM questions WHERE category_id = categories.id AND is_visible = true) WHERE id IS NOT NULL;

-- 更新题库的问题计数
UPDATE question_banks SET question_count = (SELECT COUNT(*) FROM questions WHERE category_id IN (SELECT category_id FROM bank_category_relation WHERE bank_id = question_banks.id)) WHERE id IS NOT NULL;

-- 更新标签的问题计数
UPDATE tags SET question_count = (SELECT COUNT(*) FROM question_tags WHERE tag_id = tags.id) WHERE id IS NOT NULL;
