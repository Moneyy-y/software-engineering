package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.BusinessException;
import com.catering.common.PageResult;
import com.catering.context.UserContext;
import com.catering.dto.FeedbackSubmitDTO;
import com.catering.entity.Feedback;
import com.catering.mapper.FeedbackMapper;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;

    public FeedbackService(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    public void submit(FeedbackSubmitDTO dto) {
        if (!StringUtils.hasText(dto.getDescription()) || dto.getDescription().length() < 10) {
            throw new BusinessException(1001, "反馈描述至少10字");
        }
        Feedback fb = new Feedback();
        fb.setUserId(UserContext.getUserId());
        fb.setType(dto.getType() != null ? dto.getType() : "other");
        fb.setDescription(dto.getDescription());
        fb.setImages(dto.getImages() != null ? JSON.toJSONString(dto.getImages()) : "[]");
        fb.setStatus("pending");
        feedbackMapper.insert(fb);
    }

    public PageResult<Map<String, Object>> list(String status, int page, int size) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) wrapper.eq(Feedback::getStatus, status);
        wrapper.orderByDesc(Feedback::getCreateTime);
        long total = feedbackMapper.selectCount(wrapper);
        List<Feedback> list = feedbackMapper.selectList(wrapper.last("LIMIT " + (page - 1) * size + "," + size));
        List<Map<String, Object>> records = list.stream().map(this::toMap).collect(Collectors.toList());
        return new PageResult<>(records, total, page, size);
    }

    public List<Map<String, Object>> myFeedbacks() {
        return feedbackMapper.selectList(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getUserId, UserContext.getUserId())
                .orderByDesc(Feedback::getCreateTime))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public void accept(Long id) {
        Feedback fb = get(id);
        fb.setStatus("processing");
        fb.setHandlerId(UserContext.getUserId());
        fb.setAcceptTime(LocalDateTime.now());
        feedbackMapper.updateById(fb);
    }

    public void reply(Long id, String content) {
        Feedback fb = get(id);
        fb.setReply(content);
        feedbackMapper.updateById(fb);
    }

    public void close(Long id) {
        Feedback fb = get(id);
        fb.setStatus("resolved");
        fb.setResolveTime(LocalDateTime.now());
        feedbackMapper.updateById(fb);
    }

    private Feedback get(Long id) {
        Feedback fb = feedbackMapper.selectById(id);
        if (fb == null) throw new BusinessException("反馈不存在");
        return fb;
    }

    private Map<String, Object> toMap(Feedback fb) {
        Map<String, Object> m = new HashMap<>();
        m.put("feedbackId", fb.getFeedbackId());
        m.put("type", fb.getType());
        m.put("description", fb.getDescription());
        m.put("status", fb.getStatus());
        m.put("reply", fb.getReply());
        m.put("createTime", fb.getCreateTime());
        m.put("resolveTime", fb.getResolveTime());
        return m;
    }
}
