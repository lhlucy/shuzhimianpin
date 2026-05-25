// dto/request/ReportRequest.java
package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReportRequest {
    @NotBlank(message = "目标类型不能为空")
    private String targetType; // QUESTION, ANSWER, COMMENT, USER

    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @NotBlank(message = "举报原因不能为空")
    private String reason; // SPAM, ABUSE, INAPPROPRIATE, COPYRIGHT, OTHER

    private String description;
}
