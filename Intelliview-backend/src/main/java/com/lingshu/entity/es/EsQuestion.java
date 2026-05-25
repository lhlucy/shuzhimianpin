package com.lingshu.entity.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "questions")
@Data
public class EsQuestion {  // 可以添加 @Setting 注解

    @Id
    private Long id;

    // 使用 standard 分析器，支持ES的高级搜索
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String solution;

    @Field(type = FieldType.Keyword)  // 精确匹配
    private String difficulty;

    @Field(type = FieldType.Text)
    private String difficultyLabel;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Text)
    private String categoryName;

    @Field(type = FieldType.Keyword)  // 标签列表，精确匹配
    private List<String> tags;

    @Field(type = FieldType.Integer)
    private Integer markCount = 0;

    @Field(type = FieldType.Integer)
    private Integer browseCount = 0;

    @Field(type = FieldType.Integer)
    private Integer viewCount = 0;

    @Field(type = FieldType.Integer)
    private Integer submitCount = 0;

    @Field(type = FieldType.Integer)
    private Integer acceptCount = 0;

    @Field(type = FieldType.Double)
    private BigDecimal acceptRate = BigDecimal.ZERO;

    @Field(type = FieldType.Boolean)
    private Boolean isVisible = true;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Date)
    private LocalDateTime lastBrowseTime;
}
