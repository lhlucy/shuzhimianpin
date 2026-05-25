// dto/request/CommentRequest.java
package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CommentRequest {
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 2, max = 500, message = "评论内容长度为2-500个字符")
    private String content;

    private Long parentId; // 父评论ID，用于回复评论
}
