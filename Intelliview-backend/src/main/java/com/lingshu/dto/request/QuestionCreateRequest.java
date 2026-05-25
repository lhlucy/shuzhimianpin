// dto/request/QuestionCreateRequest.java
package com.lingshu.dto.request;

import com.lingshu.dto.KeyPoint;
import lombok.Data;
import javax.validation.constraints.*;
import java.util.*;

@Data
public class QuestionCreateRequest {
    // 基本信息
    @NotBlank(message = "题目标题不能为空")
    @Size(max = 200, message = "题目标题最多200字")
    private String title;

    @Size(max = 500, message = "题目描述最多500字")
    private String description;

    @NotBlank(message = "题目内容不能为空")
    private String questionText;

    private String answerText;  // 答案内容/解析

    // 知识点（用于AI评估）
    private List<KeyPoint> keyPoints = new ArrayList<>();

    // 难度级别
    @NotNull(message = "难度级别不能为空")
    @Pattern(regexp = "EASY|MEDIUM|HARD", message = "难度级别必须是EASY, MEDIUM或HARD")
    private String difficulty = "MEDIUM";

    private Long categoryId;

    private String questionType = "TECHNICAL";

    private Long primaryJobRoleId;

    private String primaryJobRoleCode;

    @Size(max = 500, message = "能力项摘要最多500字")
    private String skillDimensionSummary;

    private List<String> keywords = new ArrayList<>();

    private List<String> standardAnswerPoints = new ArrayList<>();

    private List<String> commonMistakes = new ArrayList<>();

    private List<String> followUpPrompts = new ArrayList<>();

    private List<String> scoringPoints = new ArrayList<>();

    private List<String> recommendedResources = new ArrayList<>();

    private List<String> tags = new ArrayList<>();  // 标签名称列表

    // 相关题目ID
    private List<Long> relatedQuestionIds = new ArrayList<>();

    // 元数据（扩展字段）
    private Map<String, Object> metadata = new HashMap<>();

    // 其他
    private Boolean isForInterview = true;
    private Boolean isForPractice = true;
    private Integer interviewFrequency = 0;
    private String sourceType = "ADMIN";
    private Boolean isVisible = true;
    private Integer sortOrder = 0;
}
