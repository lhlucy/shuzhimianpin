package com.lingshu.service.impl;

import com.lingshu.entity.AIInterview;
import com.lingshu.entity.AIInterviewAnswer;
import com.lingshu.entity.AIInterviewAssessment;
import com.lingshu.entity.EvaluationCalibration;
import com.lingshu.mapper.EvaluationCalibrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationCalibrationService {

    private final EvaluationCalibrationMapper calibrationMapper;

    public void recordInterviewCalibration(AIInterview interview, AIInterviewAssessment assessment, List<AIInterviewAnswer> answers) {
        if (interview == null || assessment == null || answers == null || answers.isEmpty()) {
            return;
        }

        List<Double> scores = answers.stream()
                .map(AIInterviewAnswer::getScore)
                .filter(score -> score != null)
                .toList();
        if (scores.isEmpty()) {
            return;
        }

        double expected = scores.stream().mapToDouble(Double::doubleValue).average().orElse(assessment.getOverallScore());
        double actual = assessment.getOverallScore() == null ? expected : assessment.getOverallScore();
        double variance = variance(scores, expected);
        double biasRate = expected == 0 ? 0.0 : Math.abs(actual - expected) / expected;

        EvaluationCalibration record = new EvaluationCalibration();
        record.setInterviewId(interview.getId());
        record.setJobRoleId(interview.getJobRoleId());
        record.setSourceType("INTERVIEW_SUMMARY");
        record.setEvaluationRunCount(scores.size());
        record.setExpectedScore(round(expected));
        record.setAiScore(round(actual));
        record.setScoreVariance(round(variance));
        record.setBiasRate(round(biasRate));
        record.setDimensionScores(StringUtils.hasText(assessment.getSectionScores()) ? assessment.getSectionScores() : "{}");
        record.setCalibrationStatus(biasRate <= 0.15 && variance <= 250 ? "PASS" : "REVIEW");
        record.setCreatedAt(LocalDateTime.now());
        calibrationMapper.insert(record);
    }

    private double variance(List<Double> values, double average) {
        return values.stream()
                .mapToDouble(value -> Math.pow(value - average, 2))
                .average()
                .orElse(0.0);
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
