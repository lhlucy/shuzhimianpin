package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIInterviewTranscriptionResponse {

    private String transcript;

    private String fileName;

    private String mimeType;
}
