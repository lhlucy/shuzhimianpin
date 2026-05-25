package com.lingshu.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 刷题历史响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeHistoryResponse {
    
    private Long id;
    
    private Long questionId;
    
    private String questionTitle;
    
    private String questionSlug;
    
    private String difficulty;
    
    private String difficultyLabel;
    
    private Boolean completed;
    
    private Boolean viewAnswer;
    
    private Integer duration;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
