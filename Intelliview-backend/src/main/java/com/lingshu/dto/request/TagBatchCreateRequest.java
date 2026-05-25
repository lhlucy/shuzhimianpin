package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TagBatchCreateRequest {

    @NotNull(message = "标签列表不能为空")
    @NotEmpty(message = "标签列表不能为空")
    private List<@Valid TagCreateRequest> tags;
}
