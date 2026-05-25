package com.lingshu.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lingshu.entity.VerificationCode;
import com.lingshu.mapper.VerificationCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupTask {

    private final VerificationCodeMapper verificationCodeMapper;

    /**
     * 清理过期的验证码
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredVerificationCodes() {
        LocalDateTime now = LocalDateTime.now();

        // 查询过期且未使用的验证码
        QueryWrapper<VerificationCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("expire_time", now)
                .eq("used", false);

        // 统计要删除的数量
        long deletedCount = verificationCodeMapper.selectCount(queryWrapper);

        if (deletedCount > 0) {
            // 执行删除操作
            verificationCodeMapper.delete(queryWrapper);
            log.info("清理了 {} 个过期验证码", deletedCount);
        }
    }
}
