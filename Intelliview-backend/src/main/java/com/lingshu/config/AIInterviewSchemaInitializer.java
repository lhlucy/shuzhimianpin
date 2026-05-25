package com.lingshu.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIInterviewSchemaInitializer implements ApplicationRunner {

    private static final String TABLE_NAME = "ai_interviews";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists(TABLE_NAME)) {
            log.warn("表 {} 不存在，跳过模拟面试增量字段检查", TABLE_NAME);
            return;
        }

        ensureColumn(
                "interview_language",
                "ALTER TABLE ai_interviews ADD COLUMN interview_language varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '中文' AFTER target_position"
        );
        ensureColumn(
                "brushed_question_ids",
                "ALTER TABLE ai_interviews ADD COLUMN brushed_question_ids json NULL AFTER skill_tags"
        );
        ensureColumn(
                "brushed_question_summary",
                "ALTER TABLE ai_interviews ADD COLUMN brushed_question_summary json NULL AFTER brushed_question_ids"
        );
        ensureColumn(
                "resume_file_name",
                "ALTER TABLE ai_interviews ADD COLUMN resume_file_name varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER voice_enabled"
        );
        ensureColumn(
                "resume_content",
                "ALTER TABLE ai_interviews ADD COLUMN resume_content text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER resume_file_name"
        );
        ensureColumn(
                "interviewer_profile",
                "ALTER TABLE ai_interviews ADD COLUMN interviewer_profile text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER resume_content"
        );
        ensureColumn(
                "opening_message",
                "ALTER TABLE ai_interviews ADD COLUMN opening_message text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER interviewer_profile"
        );
    }

    private void ensureColumn(String columnName, String ddl) {
        if (columnExists(TABLE_NAME, columnName)) {
            return;
        }
        jdbcTemplate.execute(ddl);
        log.info("已自动补齐 ai_interviews.{} 字段", columnName);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }
}
