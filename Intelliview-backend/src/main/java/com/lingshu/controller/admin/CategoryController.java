package com.lingshu.controller.admin;

import com.lingshu.dto.request.CategoryCreateRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.CategoryTreeNode;
import com.lingshu.entity.Category;
import com.lingshu.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建分类
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {

        Category category = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", category));
    }

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeNode>>> getCategoryTree() {
        List<CategoryTreeNode> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    /**
     * 获取所有分类（扁平结构）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 根据ID获取分类
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    /**
     * 根据父ID获取分类
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<Category>>> getCategoriesByParentId(
            @PathVariable Long parentId) {

        List<Category> categories = categoryService.getCategoriesByParentId(parentId);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
