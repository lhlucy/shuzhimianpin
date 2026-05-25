package com.lingshu.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingshu.entity.InterviewTemplate;
import com.lingshu.entity.JobRole;
import com.lingshu.entity.Question;
import com.lingshu.entity.User;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.InterviewTemplateMapper;
import com.lingshu.mapper.JobRoleMapper;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminJobRoleService {

    private final JobRoleMapper jobRoleMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final InterviewTemplateMapper interviewTemplateMapper;

    public List<JobRole> listRoles(Boolean activeOnly, String keyword) {
        LambdaQueryWrapper<JobRole> wrapper = new LambdaQueryWrapper<JobRole>()
                .orderByAsc(JobRole::getId);

        if (activeOnly != null) {
            wrapper.eq(JobRole::getIsActive, activeOnly);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(JobRole::getName, keyword.trim())
                    .or()
                    .like(JobRole::getCode, keyword.trim())
                    .or()
                    .like(JobRole::getDescription, keyword.trim()));
        }
        return jobRoleMapper.selectList(wrapper);
    }

    @Transactional
    public JobRole createRole(JobRole request) {
        validateRequiredRoleFields(request);
        ensureCodeUnique(request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        request.setId(null);
        request.setCode(request.getCode().trim());
        request.setIsActive(request.getIsActive() == null || request.getIsActive());
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        jobRoleMapper.insert(request);
        return request;
    }

    @Transactional
    public JobRole updateRole(Long id, JobRole request) {
        JobRole existing = requireRole(id);
        validateRequiredRoleFields(request);
        ensureCodeUnique(request.getCode(), id);

        existing.setCode(request.getCode().trim());
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setApplicableExperience(request.getApplicableExperience());
        existing.setCategory(request.getCategory());
        existing.setDifficultyLevel(request.getDifficultyLevel());
        existing.setTypicalTechStack(request.getTypicalTechStack());
        existing.setInterviewFocus(request.getInterviewFocus());
        existing.setIsActive(request.getIsActive() == null ? existing.getIsActive() : request.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());

        jobRoleMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void deactivateRole(Long id) {
        requireRole(id);
        jobRoleMapper.update(null, new LambdaUpdateWrapper<JobRole>()
                .eq(JobRole::getId, id)
                .set(JobRole::getIsActive, false)
                .set(JobRole::getUpdatedAt, LocalDateTime.now()));
    }

    public long countUsage(Long id) {
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getTargetJobRoleId, id));
        long questionCount = questionMapper.selectCount(new LambdaQueryWrapper<Question>()
                .eq(Question::getPrimaryJobRoleId, id));
        long templateCount = interviewTemplateMapper.selectCount(new LambdaQueryWrapper<InterviewTemplate>()
                .eq(InterviewTemplate::getJobRoleId, id));
        return userCount + questionCount + templateCount;
    }

    private JobRole requireRole(Long id) {
        JobRole role = jobRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "岗位不存在");
        }
        return role;
    }

    private void validateRequiredRoleFields(JobRole request) {
        if (!StringUtils.hasText(request.getCode()) || !StringUtils.hasText(request.getName())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "岗位编码和名称不能为空");
        }
    }

    private void ensureCodeUnique(String code, Long currentId) {
        JobRole existing = jobRoleMapper.findByCode(code.trim());
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "岗位编码已存在");
        }
    }
}
