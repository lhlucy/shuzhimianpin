// controller/FavoriteController.java
package com.lingshu.controller;


import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.QuestionResponse;
import com.lingshu.service.FavoriteService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final SecurityUtil securityUtil;

    /**
     * 添加收藏
     */
    @PostMapping("/{questionId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(@PathVariable Long questionId) {
        Long userId = securityUtil.getCurrentUserId();
        favoriteService.addFavorite(userId, questionId);

        return ResponseEntity.ok(ApiResponse.success("收藏成功", null));
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long questionId) {
        Long userId = securityUtil.getCurrentUserId();
        favoriteService.removeFavorite(userId, questionId);

        return ResponseEntity.ok(ApiResponse.success("取消收藏成功", null));
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> getUserFavorites(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = securityUtil.getCurrentUserId();
        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> favorites = favoriteService.getUserFavorites(userId, page, size);

        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    /**
     * 检查是否收藏
     */
    @GetMapping("/{questionId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(@PathVariable Long questionId) {
        Long userId = securityUtil.getCurrentUserId();
        boolean isFavorite = favoriteService.isFavorite(userId, questionId);

        return ResponseEntity.ok(ApiResponse.success(isFavorite));
    }

    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getFavoriteCount() {
        Long userId = securityUtil.getCurrentUserId();
        Long count = favoriteService.getUserFavoriteCount(userId);

        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
