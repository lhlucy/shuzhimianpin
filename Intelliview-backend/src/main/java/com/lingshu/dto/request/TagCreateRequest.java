package com.lingshu.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TagCreateRequest {
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称最多50字")
    private String name;

    @Size(max = 255, message = "标签描述最多255字")
    private String description;

    private String color;
}
