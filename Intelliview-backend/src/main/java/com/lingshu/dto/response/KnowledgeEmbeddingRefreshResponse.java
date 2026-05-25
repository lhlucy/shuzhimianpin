package com.lingshu.dto.response;

import lombok.Data;

@Data
public class KnowledgeEmbeddingRefreshResponse {

    private int chunkCount;
    private String embeddingModel;
    private int embeddingDimension;
}
