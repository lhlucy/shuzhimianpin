package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class AIInterviewCreateRequest {

    private Long jobRoleId;

    @Size(max = 64, message = "请求标识不能超过64个字符")
    private String clientRequestId;

    @Size(max = 20, message = "面试模式不能超过20个字符")
    private String interviewMode;

    @NotBlank(message = "面试名称不能为空")
    @Size(max = 100, message = "面试名称不能超过100个字符")
    private String interviewName;

    @NotBlank(message = "岗位不能为空")
    @Size(max = 100, message = "岗位名称不能超过100个字符")
    private String targetPosition;

    @NotBlank(message = "面试语言不能为空")
    @Size(max = 30, message = "面试语言不能超过30个字符")
    private String interviewLanguage;

    @Size(max = 12, message = "技术栈数量不能超过12个")
    private List<String> techStacks;

    @Size(max = 20, message = "刷题记录选择不能超过20项")
    private List<Long> practicedQuestionIds;

    @Size(max = 255, message = "简历文件名过长")
    private String resumeFileName;

    @Size(max = 20000, message = "简历内容不能超过20000个字符")
    private String resumeContent;

    private Boolean voiceEnabled;
}
