package com.lingshu.service;

import com.lingshu.dto.request.AIInterviewAnswerRequest;
import com.lingshu.dto.request.AIInterviewCreateRequest;
import com.lingshu.dto.response.AIInterviewAnswerResponse;
import com.lingshu.dto.response.AIInterviewHistoryItemResponse;
import com.lingshu.dto.response.AIInterviewQuestionResponse;
import com.lingshu.dto.response.AIInterviewResumeParseResponse;
import com.lingshu.dto.response.AIInterviewResponse;
import com.lingshu.dto.response.AIInterviewSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AIInterviewService {

    AIInterviewResponse createInterview(AIInterviewCreateRequest request);

    AIInterviewResumeParseResponse parseResume(MultipartFile file);

    AIInterviewResponse startInterview(Long interviewId);

    AIInterviewResponse getInterview(Long interviewId);

    List<AIInterviewHistoryItemResponse> listHistory(Integer limit);

    void deleteInterview(Long interviewId);

    AIInterviewQuestionResponse getNextQuestion(Long interviewId);

    AIInterviewAnswerResponse submitAnswer(Long interviewId, Long questionId, AIInterviewAnswerRequest request);

    AIInterviewSummaryResponse endInterview(Long interviewId);

    AIInterviewSummaryResponse getSummary(Long interviewId);
}
