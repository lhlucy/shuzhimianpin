package com.lingshu.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class QuestionRequest {
    @NotBlank(message = "题目标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "题目内容不能为空")
    private String questionText;

    private String answerText;

    @NotNull(message = "难度级别不能为空")
    private String difficulty; // EASY, MEDIUM, HARD

    private Long categoryId;

    private Long primaryJobRoleId;

    private String primaryJobRoleCode;

    private List<Long> tagIds;

    private Boolean isVisible = true;

    private Boolean isForPractice = true;

    private Boolean isForInterview = true;

    private Integer sortOrder = 0;
}
