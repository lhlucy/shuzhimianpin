package com.lingshu.controller;

import com.lingshu.dto.request.AIInterviewAnswerRequest;
import com.lingshu.dto.request.AIInterviewAvatarSpeakRequest;
import com.lingshu.dto.request.AIInterviewCreateRequest;
import com.lingshu.dto.request.AIInterviewSpeechSynthesisRequest;
import com.lingshu.dto.response.AIInterviewAnswerResponse;
import com.lingshu.dto.response.AIInterviewAvatarSessionResponse;
import com.lingshu.dto.response.AIInterviewHistoryItemResponse;
import com.lingshu.dto.response.AIInterviewQuestionResponse;
import com.lingshu.dto.response.AIInterviewResumeParseResponse;
import com.lingshu.dto.response.AIInterviewResponse;
import com.lingshu.dto.response.AIInterviewSpeechResponse;
import com.lingshu.dto.response.AIInterviewSummaryResponse;
import com.lingshu.dto.response.AIInterviewTranscriptionResponse;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.service.AIInterviewAvatarService;
import com.lingshu.service.AIInterviewService;
import com.lingshu.service.AIInterviewSpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/interviews/ai")
@RequiredArgsConstructor
public class AIInterviewController {

    private final AIInterviewService aiInterviewService;
    private final AIInterviewSpeechService aiInterviewSpeechService;
    private final AIInterviewAvatarService aiInterviewAvatarService;

    @PostMapping("/personalized")
    public ResponseEntity<ApiResponse<AIInterviewResponse>> createInterview(@Valid @RequestBody AIInterviewCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.createInterview(request)));
    }

    @PostMapping(value = "/resume/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AIInterviewResumeParseResponse>> parseResume(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.parseResume(file)));
    }

    @PostMapping(value = "/voice/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AIInterviewTranscriptionResponse>> transcribeVoice(@RequestParam("file") MultipartFile file,
                                                                                          @RequestParam(value = "interviewLanguage", required = false) String interviewLanguage) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewSpeechService.transcribe(file, interviewLanguage)));
    }

    @PostMapping("/voice/synthesize")
    public ResponseEntity<ApiResponse<AIInterviewSpeechResponse>> synthesizeSpeech(@Valid @RequestBody AIInterviewSpeechSynthesisRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewSpeechService.synthesize(request.getText(), request.getInterviewLanguage())));
    }

    @PostMapping("/{interviewId}/start")
    public ResponseEntity<ApiResponse<AIInterviewResponse>> startInterview(@PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.startInterview(interviewId)));
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<ApiResponse<AIInterviewResponse>> getInterview(@PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.getInterview(interviewId)));
    }

    @GetMapping("/{interviewId}/avatar/session")
    public ResponseEntity<ApiResponse<AIInterviewAvatarSessionResponse>> initAvatarSession(@PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewAvatarService.initSession(interviewId)));
    }

    @PostMapping("/{interviewId}/avatar/speak")
    public ResponseEntity<ApiResponse<Boolean>> speakAvatar(@PathVariable Long interviewId,
                                                            @Valid @RequestBody AIInterviewAvatarSpeakRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewAvatarService.speak(interviewId, request.getText())));
    }

    @DeleteMapping("/{interviewId}/avatar/session")
    public ResponseEntity<ApiResponse<Boolean>> stopAvatarSession(@PathVariable Long interviewId) {
        aiInterviewAvatarService.stopSession(interviewId);
        return ResponseEntity.ok(ApiResponse.success(Boolean.TRUE));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AIInterviewHistoryItemResponse>>> listHistory(@RequestParam(value = "limit", required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.listHistory(limit)));
    }

    @DeleteMapping("/{interviewId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteInterview(@PathVariable Long interviewId) {
        aiInterviewService.deleteInterview(interviewId);
        return ResponseEntity.ok(ApiResponse.success(Boolean.TRUE));
    }

    @GetMapping("/{interviewId}/questions/next")
    public ResponseEntity<ApiResponse<AIInterviewQuestionResponse>> getNextQuestion(@PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.getNextQuestion(interviewId)));
    }

    @PostMapping("/{interviewId}/questions/{questionId}/answers")
    public ResponseEntity<ApiResponse<AIInterviewAnswerResponse>> submitAnswer(@PathVariable Long interviewId,
                                                                               @PathVariable Long questionId,
                                                                               @Valid @RequestBody AIInterviewAnswerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.submitAnswer(interviewId, questionId, request)));
    }

    @PostMapping("/{interviewId}/end")
    public ResponseEntity<ApiResponse<AIInterviewSummaryResponse>> endInterview(@PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.endInterview(interviewId)));
    }

    @GetMapping("/{interviewId}/summary")
    public ResponseEntity<ApiResponse<AIInterviewSummaryResponse>> getSummary(@PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(aiInterviewService.getSummary(interviewId)));
    }
}
