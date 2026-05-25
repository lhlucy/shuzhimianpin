package com.lingshu.service;

import com.lingshu.dto.response.UserResumeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserResumeService {

    List<UserResumeResponse> listResumes(Long userId);

    UserResumeResponse uploadResume(Long userId, MultipartFile file);

    void deleteResume(Long userId, Long resumeId);
}
