package com.lingshu.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingshu.entity.InterviewFollowUpRule;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.InterviewFollowUpRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEvaluationRuleService {

    private final InterviewFollowUpRuleMapper interviewFollowUpRuleMapper;

    public List<InterviewFollowUpRule> listRules(Long questionId, String triggerType) {
        LambdaQueryWrapper<InterviewFollowUpRule> wrapper = new LambdaQueryWrapper<InterviewFollowUpRule>()
                .orderByDesc(InterviewFollowUpRule::getUpdatedAt)
                .orderByDesc(InterviewFollowUpRule::getId);
        if (questionId != null) {
            wrapper.eq(InterviewFollowUpRule::getQuestionId, questionId);
        }
        if (StringUtils.hasText(triggerType)) {
            wrapper.eq(InterviewFollowUpRule::getTriggerType, triggerType.trim().toUpperCase());
        }
        return interviewFollowUpRuleMapper.selectList(wrapper);
    }

    @Transactional
    public InterviewFollowUpRule createRule(InterviewFollowUpRule request) {
        validateRule(request);
        LocalDateTime now = LocalDateTime.now();
        request.setId(null);
        request.setTriggerType(request.getTriggerType().trim().toUpperCase());
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        interviewFollowUpRuleMapper.insert(request);
        return request;
    }

    @Transactional
    public InterviewFollowUpRule updateRule(Long id, InterviewFollowUpRule request) {
        InterviewFollowUpRule existing = requireRule(id);
        validateRule(request);
        existing.setQuestionId(request.getQuestionId());
        existing.setTriggerType(request.getTriggerType().trim().toUpperCase());
        existing.setRuleContent(request.getRuleContent());
        existing.setUpdatedAt(LocalDateTime.now());
        interviewFollowUpRuleMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void deleteRule(Long id) {
        requireRule(id);
        interviewFollowUpRuleMapper.deleteById(id);
    }

    private InterviewFollowUpRule requireRule(Long id) {
        InterviewFollowUpRule item = interviewFollowUpRuleMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "评估规则不存在");
        }
        return item;
    }

    private void validateRule(InterviewFollowUpRule request) {
        if (request.getQuestionId() == null || !StringUtils.hasText(request.getTriggerType()) || !StringUtils.hasText(request.getRuleContent())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "题目、触发类型和规则内容不能为空");
        }
    }
}
