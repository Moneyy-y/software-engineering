package com.catering.service;

import com.catering.entity.Message;
import com.catering.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public void sendMessage(Long userId, String title, String content, String type) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    public List<Message> getMessages(Long userId) {
        return messageMapper.selectByUserId(userId);
    }

    public int getUnreadCount(Long userId) {
        return messageMapper.countUnread(userId);
    }

    public void markAsRead(Long userId) {
        messageMapper.markAsRead(userId);
    }

    public void deleteMessage(Long messageId) {
        messageMapper.deleteById(messageId);
    }

    public void deleteAll(Long userId) {
        messageMapper.deleteByUserId(userId);
    }
}