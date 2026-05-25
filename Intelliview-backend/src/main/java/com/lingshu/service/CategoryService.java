package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.lingshu.dto.request.CategoryCreateRequest;
import com.lingshu.dto.response.CategoryTreeNode;
import com.lingshu.entity.Category;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.CategoryMapper;
import com.lingshu.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 创建分类
     */
    @Transactional
    public Category createCategory(CategoryCreateRequest request) {
        // 检查分类名称是否已存在
        QueryWrapper<Category> nameWrapper = new QueryWrapper<>();
        nameWrapper.eq("name", request.getName());
        if (categoryMapper.selectOne(nameWrapper) != null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "分类名称已存在");
        }

        // 检查父分类是否存在
        if (request.getParentId() != null) {
            Category parentCategory = categoryMapper.selectById(request.getParentId());
            if (parentCategory == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND.getCode(), "父分类不存在");
            }
        }

        // 创建分类
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(SlugUtil.toSlug(request.getName()));
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());
        category.setIsVisible(request.getIsVisible());

        categoryMapper.insert(category);
        return category;
    }

    /**
     * 获取分类树
     */
    public List<CategoryTreeNode> getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectList(null);

        // 构建ID到节点的映射
        Map<Long, CategoryTreeNode> nodeMap = new HashMap<>();
        List<CategoryTreeNode> roots = new ArrayList<>();

        // 遍历所有分类创建节点
        for (Category category : allCategories) {
            CategoryTreeNode node = CategoryTreeNode.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .slug(category.getSlug())
                    .description(category.getDescription())
                    .icon(category.getIcon())
                    .parentId(category.getParentId())
                    .sortOrder(category.getSortOrder())
                    .questionCount(category.getQuestionCount())
                    .isVisible(category.getIsVisible())
                    .children(new ArrayList<>())
                    .build();

            nodeMap.put(category.getId(), node);

            if (category.getParentId() == null) {
                roots.add(node);
            }
        }

        // 构建分类树结构
        for (Category category : allCategories) {
            if (category.getParentId() != null) {
                CategoryTreeNode parent = nodeMap.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(nodeMap.get(category.getId()));
                }
            }
        }

        // 对每个节点的子节点按排序顺序排序
        for (CategoryTreeNode node : nodeMap.values()) {
            if (node.getChildren() != null) {
                node.getChildren().sort((a, b) -> {
                    if (a.getSortOrder() == null) return 1;
                    if (b.getSortOrder() == null) return -1;
                    return a.getSortOrder().compareTo(b.getSortOrder());
                });
            }
        }

        // 对根节点按排序顺序排序
        roots.sort((a, b) -> {
            if (a.getSortOrder() == null) return 1;
            if (b.getSortOrder() == null) return -1;
            return a.getSortOrder().compareTo(b.getSortOrder());
        });

        return roots;
    }

    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(null);
    }

    /**
     * 根据ID获取分类
     */
    public Category getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND.getCode(), "分类不存在");
        }
        return category;
    }

    /**
     * 根据父ID获取分类
     */
    public List<Category> getCategoriesByParentId(Long parentId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId)
                .orderByAsc("sort_order");
        return categoryMapper.selectList(queryWrapper);
    }

    /**
     * 获取所有可见分类
     */
    public List<Category> getAllVisibleCategories() {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_visible", true)
                .orderByAsc("sort_order");
        return categoryMapper.selectList(queryWrapper);
    }

    /**
     * 更新分类
     */
    @Transactional
    public Category updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND.getCode(), "分类不存在");
        }

        // 检查分类名称是否已存在（排除当前分类）
        QueryWrapper<Category> nameWrapper = new QueryWrapper<>();
        nameWrapper.eq("name", request.getName())
                .ne("id", id);
        if (categoryMapper.selectOne(nameWrapper) != null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "分类名称已存在");
        }

        // 检查父分类是否存在
        if (request.getParentId() != null) {
            Category parentCategory = categoryMapper.selectById(request.getParentId());
            if (parentCategory == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND.getCode(), "父分类不存在");
            }
        }

        // 更新分类
        category.setName(request.getName());
        category.setSlug(SlugUtil.toSlug(request.getName()));
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());
        category.setIsVisible(request.getIsVisible());

        categoryMapper.updateById(category);
        return category;
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        // 检查分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND.getCode(), "分类不存在");
        }

        // 检查是否有子分类
        QueryWrapper<Category> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", id);
        if (categoryMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "该分类下存在子分类，无法删除");
        }

        categoryMapper.deleteById(id);
    }
}

