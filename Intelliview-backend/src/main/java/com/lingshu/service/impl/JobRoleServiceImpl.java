package com.lingshu.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.dto.response.JobRoleConfigResponse;
import com.lingshu.dto.response.JobRoleOptionResponse;
import com.lingshu.entity.InterviewTemplate;
import com.lingshu.entity.JobRole;
import com.lingshu.entity.JobRoleSkillDimension;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.InterviewTemplateMapper;
import com.lingshu.mapper.JobRoleMapper;
import com.lingshu.mapper.JobRoleSkillDimensionMapper;
import com.lingshu.service.JobRoleService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JobRoleServiceImpl implements JobRoleService {

    private final JobRoleMapper jobRoleMapper;
    private final JobRoleSkillDimensionMapper skillDimensionMapper;
    private final InterviewTemplateMapper interviewTemplateMapper;
    private final ObjectMapper objectMapper;

    public JobRoleServiceImpl(
            JobRoleMapper jobRoleMapper,
            JobRoleSkillDimensionMapper skillDimensionMapper,
            InterviewTemplateMapper interviewTemplateMapper,
            ObjectMapper objectMapper
    ) {
        this.jobRoleMapper = jobRoleMapper;
        this.skillDimensionMapper = skillDimensionMapper;
        this.interviewTemplateMapper = interviewTemplateMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<JobRoleOptionResponse> listActiveRoles() {
        return jobRoleMapper.findActiveRoles().stream().map(this::buildRoleOption).collect(Collectors.toList());
    }

    @Override
    public JobRoleConfigResponse getRoleConfig(String code) {
        JobRole role = jobRoleMapper.findByCode(code);
        if (role == null || Boolean.FALSE.equals(role.getIsActive())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "岗位不存在或未启用");
        }

        JobRoleConfigResponse response = new JobRoleConfigResponse();
        response.setRole(buildRoleOption(role));

        List<JobRoleSkillDimension> dimensions = skillDimensionMapper.findByJobRoleId(role.getId());
        response.setSkillDimensions(dimensions.stream().map(item -> {
            JobRoleConfigResponse.SkillDimension dimension = new JobRoleConfigResponse.SkillDimension();
            dimension.setCode(item.getDimensionCode());
            dimension.setName(item.getDimensionName());
            dimension.setWeight(item.getWeight());
            dimension.setDescription(item.getDescription());
            return dimension;
        }).collect(Collectors.toList()));

        InterviewTemplate template = interviewTemplateMapper.findDefaultByJobRoleId(role.getId());
        if (template != null) {
            JobRoleConfigResponse.InterviewTemplateConfig templateConfig = new JobRoleConfigResponse.InterviewTemplateConfig();
            templateConfig.setTemplateCode(template.getTemplateCode());
            templateConfig.setName(template.getName());
            templateConfig.setInterviewType(template.getInterviewType());
            templateConfig.setDifficulty(template.getDifficulty());
            templateConfig.setDurationMinutes(template.getDurationMinutes());
            templateConfig.setDefaultQuestionCount(template.getDefaultQuestionCount());
            templateConfig.setFollowUpIntensity(template.getFollowUpIntensity());
            templateConfig.setQuestionTypeDistribution(parseIntegerMap(template.getQuestionTypeDistribution()));
            templateConfig.setDifficultyDistribution(parseIntegerMap(template.getDifficultyDistribution()));
            templateConfig.setScoringWeights(parseIntegerMap(template.getScoringWeights()));
            response.setTemplate(templateConfig);
        }

        return response;
    }

    private JobRoleOptionResponse buildRoleOption(JobRole role) {
        JobRoleOptionResponse response = new JobRoleOptionResponse();
        response.setId(role.getId());
        response.setCode(role.getCode());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setApplicableExperience(role.getApplicableExperience());
        response.setTypicalTechStack(parseStringList(role.getTypicalTechStack()));
        response.setInterviewFocus(parseStringList(role.getInterviewFocus()));
        return response;
    }

    private List<String> parseStringList(String json) {
        try {
            return json == null || json.isBlank()
                    ? Collections.emptyList()
                    : objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            if (json == null || json.isBlank()) {
                return Collections.emptyList();
            }
            return splitPlainTextList(json);
        }
    }

    private List<String> splitPlainTextList(String rawValue) {
        return rawValue == null
                ? Collections.emptyList()
                : new java.util.ArrayList<>(java.util.Arrays.stream(rawValue
                                .replace("[", "")
                                .replace("]", "")
                                .replace("\"", "")
                                .replace("'", "")
                                .split("[,，、|/;；\\n\\r]+"))
                        .map(String::trim)
                        .filter(item -> !item.isEmpty())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private Map<String, Integer> parseIntegerMap(String json) {
        try {
            return json == null || json.isBlank()
                    ? Collections.emptyMap()
                    : objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
