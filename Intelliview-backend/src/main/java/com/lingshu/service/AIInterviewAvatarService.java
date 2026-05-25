package com.lingshu.service;

import com.lingshu.dto.response.AIInterviewAvatarSessionResponse;

public interface AIInterviewAvatarService {

    AIInterviewAvatarSessionResponse initSession(Long interviewId);

    Boolean speak(Long interviewId, String text);

    void stopSession(Long interviewId);
}
