package com.lingshu.controller.admin;

import com.lingshu.dto.request.TagBatchCreateRequest;
import com.lingshu.dto.request.TagCreateRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.entity.Tag;
import com.lingshu.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class TagController {


    private final TagService tagService;

    /**
     * 创建标签
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Tag>> createTag(
            @Valid @RequestBody TagCreateRequest request) {

        Tag tag = tagService.createTag(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", tag));
    }

    /**
     * 获取所有标签
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Tag>>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 分页获取标签
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<Tag>>> getTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        com.baomidou.mybatisplus.core.metadata.IPage<Tag> tags = tagService.getTags(page, size, keyword);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 根据ID获取标签
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Tag>> getTag(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success(tag));
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Tag>>> searchTags(
            @RequestParam String keyword) {

        List<Tag> tags = tagService.searchTags(keyword);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    /**
     * 批量创建标签
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<Tag>>> batchCreateTags(
            @Valid @RequestBody TagBatchCreateRequest request) {

        List<Tag> createdTags = tagService.batchCreateTags(request.getTags());
        return ResponseEntity.ok(ApiResponse.success("批量创建成功", createdTags));
    }

}
