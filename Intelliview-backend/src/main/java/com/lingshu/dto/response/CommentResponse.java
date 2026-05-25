// dto/response/CommentResponse.java
package com.lingshu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userNickname;
    private String userAvatar;
    private Long answerId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CommentResponse> replies; // 回复列表
}
