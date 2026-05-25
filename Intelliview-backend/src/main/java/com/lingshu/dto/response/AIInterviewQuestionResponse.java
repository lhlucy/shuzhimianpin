package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIInterviewQuestionResponse {

    private Long questionId;

    private String content;

    private String type;

    private String topic;

    private Integer questionOrder;

    private Integer estimatedTime;

    private boolean followUp;

    private boolean answered;
}
