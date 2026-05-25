package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.Message;
import com.lingshu.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageMapper messageMapper;

    /**
     * 获取用户消息列表
     */
    public IPage<Message> getUserMessages(Long userId, int page, int size) {
        Page<Message> pageInfo = new Page<>(page, size);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("created_at");
        return messageMapper.selectPage(pageInfo, queryWrapper);
    }

    /**
     * 获取用户未读消息列表
     */
    public IPage<Message> getUserUnreadMessages(Long userId, int page, int size) {
        Page<Message> pageInfo = new Page<>(page, size);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("status", "UNREAD")
                .orderByDesc("created_at");
        return messageMapper.selectPage(pageInfo, queryWrapper);
    }

    /**
     * 获取用户未读消息数量
     */
    public Long getUserUnreadMessageCount(Long userId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("status", "UNREAD");
        return messageMapper.selectCount(queryWrapper);
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markMessageAsRead(Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        message.setStatus("READ");
        message.setReadAt(LocalDateTime.now());
        messageMapper.updateById(message);
    }

    /**
     * 标记所有消息为已读
     */
    @Transactional
    public void markAllMessagesAsRead(Long userId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("status", "UNREAD");
        List<Message> unreadMessages = messageMapper.selectList(queryWrapper);
        for (Message message : unreadMessages) {
            message.setStatus("READ");
            message.setReadAt(LocalDateTime.now());
            messageMapper.updateById(message);
        }
    }

    /**
     * 删除消息
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        messageMapper.deleteById(messageId);
    }

    /**
     * 清空用户消息
     */
    @Transactional
    public void clearUserMessages(Long userId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        messageMapper.delete(queryWrapper);
    }

    /**
     * 发送系统消息
     */
    @Transactional
    public void sendSystemMessage(Long userId, String title, String content, Long relatedId, String relatedType) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType("SYSTEM");
        message.setStatus("UNREAD");
        message.setRelatedId(relatedId);
        message.setRelatedType(relatedType);
        messageMapper.insert(message);
    }

    /**
     * 发送通知消息
     */
    @Transactional
    public void sendNotificationMessage(Long userId, String title, String content, Long relatedId, String relatedType) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType("NOTICE");
        message.setStatus("UNREAD");
        message.setRelatedId(relatedId);
        message.setRelatedType(relatedType);
        messageMapper.insert(message);
    }
}

