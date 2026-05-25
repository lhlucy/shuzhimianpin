package com.lingshu.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 刷题记录请求DTO
 */
@Data
public class PracticeRecordRequest {
    
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    
    private Boolean completed = false;
    
    private Boolean viewAnswer = false;
    
    private Integer duration = 0;
}
