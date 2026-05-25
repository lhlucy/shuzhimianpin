package com.lingshu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AIInterviewDimensionModelTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parseDimensionScoresShouldClampAndKeepExpectedKeys() throws Exception {
        JsonNode root = objectMapper.readTree("{\"technicalDepth\":88.24,\"projectRelevance\":101,"
                + "\"problemSolving\":77,\"communicationClarity\":-3,\"jobMatch\":82}");

        Map<String, Double> scores = AIInterviewDimensionModel.parseDimensionScores(root, 60.0);

        assertEquals(88.2, scores.get("technicalDepth"));
        assertEquals(100.0, scores.get("projectRelevance"));
        assertEquals(77.0, scores.get("problemSolving"));
        assertEquals(0.0, scores.get("communicationClarity"));
        assertEquals(82.0, scores.get("jobMatch"));
        assertTrue(AIInterviewDimensionModel.isComplete(scores));
    }

    @Test
    void averageShouldUseFallbackWhenAKeyIsMissing() {
        Map<String, Double> scores = AIInterviewDimensionModel.average(List.of(
                Map.of("technicalDepth", 80.0, "projectRelevance", 70.0),
                Map.of("technicalDepth", 90.0, "projectRelevance", 74.0)
        ), 65.0);

        assertEquals(85.0, scores.get("technicalDepth"));
        assertEquals(72.0, scores.get("projectRelevance"));
        assertEquals(65.0, scores.get("problemSolving"));
        assertEquals(65.0, scores.get("communicationClarity"));
        assertEquals(65.0, scores.get("jobMatch"));
    }
}
