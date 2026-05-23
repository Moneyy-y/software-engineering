package com.catering.service;

import com.catering.entity.Message;
import com.catering.mapper.MessageMapper;
import com.catering.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MessageMapper messageMapper;

    public void sendMessage(Long userId, String title, String content, String type) {
        sendMessage(userId, title, content, type, null, null, null);
    }

    public void sendMessage(Long userId, String title, String content, String type,
                            String relatedType, Long relatedId, Long dishId) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedType(relatedType);
        message.setRelatedId(relatedId);
        message.setDishId(dishId);
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        try {
            messageMapper.insert(message);
        } catch (DataAccessException ex) {
            if (!StringUtils.hasText(relatedType) && relatedId == null && dishId == null) {
                throw ex;
            }
            message.setRelatedType(null);
            message.setRelatedId(null);
            message.setDishId(null);
            messageMapper.insertBasic(message);
        }
    }

    public List<MessageVO> getMessages(Long userId) {
        List<Message> rows;
        try {
            rows = messageMapper.selectByUserId(userId);
        } catch (DataAccessException ex) {
            rows = messageMapper.selectByUserIdBasic(userId);
        }
        return rows.stream().map(this::toVo).collect(Collectors.toList());
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

    private boolean toReadFlag(Object isRead) {
        if (isRead == null) {
            return false;
        }
        if (isRead instanceof Boolean) {
            return (Boolean) isRead;
        }
        if (isRead instanceof Number) {
            return ((Number) isRead).intValue() != 0;
        }
        return Boolean.TRUE.equals(isRead);
    }

    private MessageVO toVo(Message m) {
        MessageVO vo = new MessageVO();
        vo.setMessageId(m.getMessageId());
        vo.setTitle(m.getTitle());
        vo.setContent(m.getContent());
        vo.setType(m.getType());
        vo.setIsRead(toReadFlag(m.getIsRead()));
        if (m.getCreateTime() != null) {
            vo.setCreateTime(m.getCreateTime().format(TIME_FMT));
        }
        vo.setRelatedType(m.getRelatedType());
        vo.setRelatedId(m.getRelatedId());
        vo.setDishId(m.getDishId());
        return vo;
    }
}
