package com.lingshu.controller;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.entity.Tag;
import com.lingshu.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class PublicTagController {

    private final TagService tagService;

    /**
     * 获取所有标签
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Tag>>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<Tag>>> getHotTags() {
        List<Tag> tags = tagService.getHotTags(20);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
}

