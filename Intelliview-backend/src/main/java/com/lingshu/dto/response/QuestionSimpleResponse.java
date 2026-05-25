package com.lingshu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSimpleResponse {
    private Long id;
    private String title;
    private String slug;
    private String difficulty;
    private String difficultyLabel;
}
