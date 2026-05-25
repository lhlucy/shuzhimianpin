package com.lingshu.service.impl;

import com.lingshu.config.AIInterviewStabilityProperties;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AIInterviewStabilityGuard {

    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);

    private final AIInterviewStabilityProperties properties;
    private final Clock clock = Clock.systemDefaultZone();
    private final ConcurrentMap<String, RateBucket> rateBuckets = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, IdempotentRecord> idempotentRecords = new ConcurrentHashMap<>();

    public Optional<Long> findReusableInterview(Long userId, String clientRequestId) {
        if (!StringUtils.hasText(clientRequestId)) {
            return Optional.empty();
        }
        cleanupIdempotentRecords();
        IdempotentRecord record = idempotentRecords.get(idempotentKey(userId, clientRequestId));
        if (record == null || record.expiresAt.isBefore(now())) {
            return Optional.empty();
        }
        return Optional.of(record.interviewId);
    }

    public void checkCreateRate(Long userId) {
        checkRate("interview:create:user:" + userId, properties.getCreateUserLimitPerMinute());
        checkRate("interview:create:global", properties.getCreateGlobalLimitPerMinute());
    }

    public void checkAnswerRate(Long userId) {
        checkRate("interview:answer:user:" + userId, properties.getAnswerUserLimitPerMinute());
    }

    public void rememberCreatedInterview(Long userId, String clientRequestId, Long interviewId) {
        if (!StringUtils.hasText(clientRequestId) || interviewId == null) {
            return;
        }
        Instant expiresAt = now().plus(Duration.ofMinutes(properties.getIdempotencyTtlMinutes()));
        idempotentRecords.put(idempotentKey(userId, clientRequestId), new IdempotentRecord(interviewId, expiresAt));
    }

    private void checkRate(String key, int limit) {
        if (limit <= 0) {
            return;
        }
        Instant current = now();
        RateBucket bucket = rateBuckets.compute(key, (ignored, existing) -> {
            if (existing == null || !current.isBefore(existing.windowStart.plus(RATE_WINDOW))) {
                return new RateBucket(current, new AtomicInteger(1));
            }
            existing.counter.incrementAndGet();
            return existing;
        });
        if (bucket.counter.get() > limit) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "操作太频繁，请稍后再试");
        }
    }

    private void cleanupIdempotentRecords() {
        Instant current = now();
        Iterator<Map.Entry<String, IdempotentRecord>> iterator = idempotentRecords.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().expiresAt.isBefore(current)) {
                iterator.remove();
            }
        }
    }

    private String idempotentKey(Long userId, String clientRequestId) {
        return userId + ":" + clientRequestId.trim();
    }

    private Instant now() {
        return clock.instant();
    }

    private static final class RateBucket {
        private final Instant windowStart;
        private final AtomicInteger counter;

        private RateBucket(Instant windowStart, AtomicInteger counter) {
            this.windowStart = windowStart;
            this.counter = counter;
        }
    }

    private static final class IdempotentRecord {
        private final Long interviewId;
        private final Instant expiresAt;

        private IdempotentRecord(Long interviewId, Instant expiresAt) {
            this.interviewId = interviewId;
            this.expiresAt = expiresAt;
        }
    }
}
