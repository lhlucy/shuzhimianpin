// dto/response/AnswerResponse.java
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
public class AnswerResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userNickname;
    private String userAvatar;
    private Long questionId;
    private String content;
    private Integer likeCount;
    private Integer viewCount;
    private Boolean isAnonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
