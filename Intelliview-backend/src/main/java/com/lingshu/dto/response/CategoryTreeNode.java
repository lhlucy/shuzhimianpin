package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CategoryTreeNode {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private Long parentId;
    private Integer sortOrder;
    private Integer questionCount;
    private Boolean isVisible;
    private List<CategoryTreeNode> children;
}
