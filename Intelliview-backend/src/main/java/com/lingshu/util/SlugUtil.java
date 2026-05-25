package com.lingshu.util;

import cn.hutool.core.util.StrUtil;

public class SlugUtil {

    public static String toSlug(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }

        // 生成友好的URL slug
        String slug = text.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-") // 非字母数字汉字替换为-
                .replaceAll("^-|-$", "") // 去除首尾的-
                .replaceAll("-+", "-"); // 多个-替换为单个-

        return slug;
    }

    public static String generateQuestionSlug(String title) {
        String baseSlug = toSlug(title);
        return baseSlug + "-" + System.currentTimeMillis();
    }
}
