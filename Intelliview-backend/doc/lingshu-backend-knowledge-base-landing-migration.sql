ALTER TABLE excellent_answer_examples
MODIFY COLUMN question_id BIGINT NULL;

ALTER TABLE knowledge_chunks
ADD COLUMN IF NOT EXISTS embedding_vector LONGTEXT NULL AFTER `embedding_model`,
ADD COLUMN IF NOT EXISTS embedding_dimension INT NULL AFTER `embedding_vector`,
ADD COLUMN IF NOT EXISTS embedding_updated_at DATETIME NULL AFTER `embedding_dimension`;

CREATE INDEX idx_knowledge_documents_role_type_status
ON knowledge_documents (job_role_id, doc_type, status);

CREATE INDEX idx_knowledge_chunks_role_document_chunk
ON knowledge_chunks (job_role_id, document_id, chunk_index);

CREATE INDEX idx_excellent_answer_examples_role_type_topic
ON excellent_answer_examples (job_role_id, question_type, topic);
