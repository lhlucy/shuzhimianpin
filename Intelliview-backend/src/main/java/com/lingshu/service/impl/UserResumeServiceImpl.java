package com.lingshu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingshu.dto.response.ResumeParseResult;
import com.lingshu.dto.response.UserResumeResponse;
import com.lingshu.entity.UserResume;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.UserResumeMapper;
import com.lingshu.service.ResumeParseService;
import com.lingshu.service.UploadStorageService;
import com.lingshu.service.UserResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserResumeServiceImpl implements UserResumeService {

    private final UserResumeMapper userResumeMapper;
    private final ResumeParseService resumeParseService;
    private final UploadStorageService uploadStorageService;

    @Override
    public List<UserResumeResponse> listResumes(Long userId) {
        return userResumeMapper.selectList(new LambdaQueryWrapper<UserResume>()
                        .eq(UserResume::getUserId, userId)
                        .orderByDesc(UserResume::getCreatedAt))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResumeResponse uploadResume(Long userId, MultipartFile file) {
        ResumeParseResult parsed = resumeParseService.parse(file);
        UploadStorageService.StoredFile stored = uploadStorageService.storeResume(userId, file);
        LocalDateTime now = LocalDateTime.now();

        UserResume resume = new UserResume();
        resume.setUserId(userId);
        resume.setFileName(stored.fileName());
        resume.setOriginalFileName(stored.originalFileName());
        resume.setFileUrl(stored.url());
        resume.setFileType(stored.fileType());
        resume.setFileSize(stored.fileSize());
        resume.setContent(parsed.getContent());
        resume.setSummary(parsed.getSummary());
        resume.setIntentionJob(parsed.getIntentionJob());
        resume.setRecruitmentType(parsed.getRecruitmentType());
        resume.setIntentionCity(parsed.getIntentionCity());
        resume.setExpectedSalary(parsed.getExpectedSalary());
        resume.setParsedAt(now);
        resume.setCreatedAt(now);
        resume.setUpdatedAt(now);
        userResumeMapper.insert(resume);
        return toResponse(resume);
    }

    @Override
    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        UserResume resume = userResumeMapper.selectById(resumeId);
        if (resume == null || !Objects.equals(resume.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "简历不存在");
        }
        userResumeMapper.deleteById(resumeId);
    }

    private UserResumeResponse toResponse(UserResume resume) {
        return UserResumeResponse.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .originalFileName(resume.getOriginalFileName())
                .fileUrl(resume.getFileUrl())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .content(resume.getContent())
                .summary(resume.getSummary())
                .intentionJob(resume.getIntentionJob())
                .recruitmentType(resume.getRecruitmentType())
                .intentionCity(resume.getIntentionCity())
                .expectedSalary(resume.getExpectedSalary())
                .parsedAt(resume.getParsedAt())
                .createdAt(resume.getCreatedAt())
                .build();
    }
}
