package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.dto.request.TagCreateRequest;
import com.lingshu.entity.Tag;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.TagMapper;
import com.lingshu.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagMapper tagMapper;

    /**
     * 创建标签
     */
    @Transactional
    public Tag createTag(TagCreateRequest request) {
        // 检查标签名是否已存在
        QueryWrapper<Tag> nameWrapper = new QueryWrapper<>();
        nameWrapper.eq("name", request.getName());
        if (tagMapper.selectOne(nameWrapper) != null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "标签名已存在");
        }

        // 创建标签
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setSlug(SlugUtil.toSlug(request.getName()));
        tag.setDescription(request.getDescription());
        tag.setColor(request.getColor());

        tagMapper.insert(tag);
        return tag;
    }

    /**
     * 获取所有标签
     */
    public List<Tag> getAllTags() {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        return tagMapper.selectList(queryWrapper);
    }

    /**
     * 分页获取标签
     */
    public IPage<Tag> getTags(int page, int size, String keyword) {
        Page<Tag> pageInfo = new Page<>(page, size);
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("name", keyword);
        }

        queryWrapper.orderByDesc("created_at");
        return tagMapper.selectPage(pageInfo, queryWrapper);
    }

    /**
     * 根据ID获取标签
     */
    public Tag getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "标签不存在");
        }
        return tag;
    }

    /**
     * 搜索标签
     */
    public List<Tag> searchTags(String keyword) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", keyword)
                .orderByDesc("created_at");
        return tagMapper.selectList(queryWrapper);
    }

    /**
     * 删除标签
     */
    @Transactional
    public void deleteTag(Long id) {
        if (tagMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "标签不存在");
        }
        tagMapper.deleteById(id);
    }

    /**
     * 批量创建标签
     */
    @Transactional
    public List<Tag> batchCreateTags(List<TagCreateRequest> tagRequests) {
        List<Tag> createdTags = new ArrayList<>();

        for (TagCreateRequest request : tagRequests) {
            try {
                // 检查标签是否已存在
                QueryWrapper<Tag> nameWrapper = new QueryWrapper<>();
                nameWrapper.eq("name", request.getName());
                Tag existingTag = tagMapper.selectOne(nameWrapper);
                if (existingTag != null) {
                    log.warn("标签已存在，跳过创建: {}", request.getName());
                    createdTags.add(existingTag);
                    continue;
                }

                // 创建标签
                Tag tag = new Tag();
                tag.setName(request.getName());
                tag.setSlug(SlugUtil.toSlug(request.getName()));
                tag.setDescription(request.getDescription());
                tag.setColor(request.getColor());

                tagMapper.insert(tag);
                createdTags.add(tag);
                log.info("标签创建成功: {}", request.getName());

            } catch (Exception e) {
                log.error("标签创建失败: {}, 错误: {}", request.getName(), e.getMessage());
                // 继续处理其他标签
            }
        }

        return createdTags;
    }

    /**
     * 获取热门标签
     */
    public List<Tag> getHotTags(int limit) {
        return tagMapper.selectHotTags(limit);
    }

    /**
     * 更新标签
     */
    @Transactional
    public Tag updateTag(Long id, TagCreateRequest request) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "标签不存在");
        }

        // 检查标签名是否已被其他标签使用
        QueryWrapper<Tag> nameWrapper = new QueryWrapper<>();
        nameWrapper.eq("name", request.getName())
                .ne("id", id);
        if (tagMapper.selectOne(nameWrapper) != null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "标签名已存在");
        }

        // 更新标签
        tag.setName(request.getName());
        tag.setSlug(SlugUtil.toSlug(request.getName()));
        tag.setDescription(request.getDescription());
        tag.setColor(request.getColor());

        tagMapper.updateById(tag);
        return tag;
    }
}

