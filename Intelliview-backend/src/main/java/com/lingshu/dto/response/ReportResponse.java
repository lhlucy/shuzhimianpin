// dto/response/ReportResponse.java
package com.lingshu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String targetType;
    private Long targetId;
    private String reason;
    private String description;
    private String status;
    private String result;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Long userId;
    private Long adminId;
}
