-- 目的：
-- 修复旧库中 ai_interview_questions 的历史字段约束导致的插入失败：
-- Field 'question_text' doesn't have a default value
--
-- 适用场景：
-- 已有库包含 question_text/question_type/sort_order/time_limit 等旧字段，
-- 且这些字段存在 NOT NULL 约束或无默认值，导致新代码仅写 content/type/question_order 时失败。

ALTER TABLE ai_interview_questions MODIFY COLUMN question_text TEXT NULL;
ALTER TABLE ai_interview_questions MODIFY COLUMN question_type VARCHAR(50) NULL;
ALTER TABLE ai_interview_questions MODIFY COLUMN sort_order INT NULL;
ALTER TABLE ai_interview_questions MODIFY COLUMN time_limit INT NULL;

UPDATE ai_interview_questions
SET question_text = COALESCE(question_text, content),
    question_type = COALESCE(question_type, type),
    sort_order = COALESCE(sort_order, question_order),
    time_limit = COALESCE(time_limit, estimated_time, 120);
