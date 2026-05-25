package com.lingshu.service;

import com.lingshu.dto.response.AIInterviewSpeechResponse;
import com.lingshu.dto.response.AIInterviewTranscriptionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AIInterviewSpeechService {

    AIInterviewTranscriptionResponse transcribe(MultipartFile file, String interviewLanguage);

    AIInterviewSpeechResponse synthesize(String text, String interviewLanguage);
}
