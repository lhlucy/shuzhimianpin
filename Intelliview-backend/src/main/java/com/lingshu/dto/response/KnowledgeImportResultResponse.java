package com.lingshu.dto.response;

import lombok.Data;

@Data
public class KnowledgeImportResultResponse {

    private int documentCreated;
    private int documentUpdated;
    private int chunkCreated;
    private int chunkUpdated;
    private int exampleCreated;
    private int exampleUpdated;
    private int chunkEmbeddingGenerated;
}
