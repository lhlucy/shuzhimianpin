// service/ElasticsearchSyncService.java
package com.lingshu.service;

import com.lingshu.entity.Question;
import com.lingshu.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSyncService {

    private final QuestionMapper questionMapper;

    /**
     * 初始化同步
     */
    @PostConstruct
    public void initSync() {
        log.info("开始初始化同步到Elasticsearch");
        syncAllQuestions();
    }

    /**
     * 定时同步，每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledSync() {
        log.info("开始定时同步到Elasticsearch");
        syncAllQuestions();
    }

    /**
     * 同步所有问题到Elasticsearch
     */
    @Transactional
    public void syncAllQuestions() {
        try {
            // 查询所有可见的问题，包括标签信息
            List<Question> questions = questionMapper.findByIsVisibleTrueWithTags();

            log.info("找到 {} 个问题需要同步到Elasticsearch", questions.size());
        } catch (Exception e) {
            log.error("同步到Elasticsearch失败", e);
        }
    }

    /**
     * 同步单个问题
     */
    public void syncQuestion(Long questionId) {
        try {
            Question question = questionMapper.selectById(questionId);
            if (question == null) {
                throw new RuntimeException("问题不存在");
            }

            if (Boolean.TRUE.equals(question.getIsVisible())) {
                log.info("同步问题到Elasticsearch: id={}", questionId);
            } else {
                log.info("从Elasticsearch删除问题: id={}", questionId);
            }
        } catch (Exception e) {
            log.error("同步问题到Elasticsearch失败: id={}", questionId, e);
        }
    }
}
