package com.lingshu.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CategoryCreateRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最多50字")
    private String name;

    @Size(max = 500, message = "分类描述最多500字")
    private String description;

    private String icon;

    private Long parentId;

    private Integer sortOrder = 0;

    private Boolean isVisible = true;
}
