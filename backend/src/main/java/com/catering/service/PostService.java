package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.BusinessException;
import com.catering.common.PageResult;
import com.catering.context.UserContext;
import com.catering.entity.Comment;
import com.catering.entity.Dish;
import com.catering.entity.Post;
import com.catering.mapper.CommentMapper;
import com.catering.mapper.DishMapper;
import com.catering.mapper.PostMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final DishMapper dishMapper;
    private final SensitiveWordService sensitiveWordService;
    private final StringRedisTemplate redisTemplate;
    private final MessageService messageService;

    public PostService(PostMapper postMapper, CommentMapper commentMapper, DishMapper dishMapper,
                       SensitiveWordService sensitiveWordService,
                       StringRedisTemplate redisTemplate,
                       MessageService messageService) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.dishMapper = dishMapper;
        this.sensitiveWordService = sensitiveWordService;
        this.redisTemplate = redisTemplate;
        this.messageService = messageService;
    }

    public void publish(String title, String content, String zone, List<String> images) {
        String hit = sensitiveWordService.findHit(title + content);
        if (hit != null) throw new BusinessException(2001, "内容包含敏感词: " + hit);
        Post post = new Post();
        post.setUserId(UserContext.getUserId());
        post.setTitle(title);
        post.setContent(content);
        post.setZone(zone != null ? zone : "general");
        if (images != null && !images.isEmpty()) {
            post.setImages(com.alibaba.fastjson.JSON.toJSONString(images));
        } else {
            post.setImages("[]");
        }
        post.setAuditStatus("pending");
        postMapper.insert(post);
        messageService.sendMessage(post.getUserId(), "帖子已提交审核",
                "您的帖子「" + title + "」已提交，审核结果将在此通知。", "post_submit",
                "post", post.getPostId(), null);
    }

    public List<Map<String, Object>> listMyPosts() {
        Long userId = UserContext.getUserId();
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, userId)
                        .orderByDesc(Post::getCreateTime))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public void resubmitPost(Long postId, String title, String content, String zone, List<String> images) {
        Long userId = UserContext.getUserId();
        Post post = postMapper.selectById(postId);
        if (post == null || !userId.equals(post.getUserId())) {
            throw new BusinessException("帖子不存在");
        }
        if (!"rejected".equals(post.getAuditStatus())) {
            throw new BusinessException(2001, "仅被拒帖子可重新提交");
        }
        String hit = sensitiveWordService.findHit(title + content);
        if (hit != null) throw new BusinessException(2001, "内容包含敏感词: " + hit);
        post.setTitle(title);
        post.setContent(content);
        post.setZone(zone != null ? zone : "general");
        if (images != null && !images.isEmpty()) {
            post.setImages(com.alibaba.fastjson.JSON.toJSONString(images));
        } else {
            post.setImages("[]");
        }
        post.setAuditStatus("pending");
        post.setRejectReason(null);
        postMapper.updateById(post);
        messageService.sendMessage(userId, "帖子已重新提交",
                "您的帖子「" + title + "」已修改并重新提交审核。", "post_submit",
                "post", postId, null);
    }

    public List<Map<String, Object>> listApproved(String zone) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getAuditStatus, "approved").orderByDesc(Post::getCreateTime);
        if (StringUtils.hasText(zone)) wrapper.eq(Post::getZone, zone);
        return postMapper.selectList(wrapper).stream().map(this::toMap).collect(Collectors.toList());
    }

    public Map<String, Object> getDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"approved".equals(post.getAuditStatus())) {
            throw new BusinessException("帖子不存在或未通过审核");
        }
        Map<String, Object> m = toMap(post);
        m.put("comments", listComments(postId));
        Long userId = UserContext.getUserId();
        if (userId != null) {
            m.put("liked", Boolean.TRUE.equals(redisTemplate.opsForSet()
                    .isMember(likeKey(postId), String.valueOf(userId))));
        }
        return m;
    }

    public List<Map<String, Object>> listComments(Long postId) {
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId).orderByAsc(Comment::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        int floor = 1;
        for (Comment c : comments) {
            Map<String, Object> m = new HashMap<>();
            m.put("commentId", c.getCommentId());
            m.put("content", c.getContent());
            m.put("createTime", c.getCreateTime());
            m.put("floor", floor++);
            result.add(m);
        }
        return result;
    }

    public void addComment(Long postId, String content) {
        String hit = sensitiveWordService.findHit(content);
        if (hit != null) throw new BusinessException(2001, "评论包含敏感词: " + hit);
        Comment c = new Comment();
        c.setPostId(postId);
        c.setUserId(UserContext.getUserId());
        c.setContent(content);
        commentMapper.insert(c);
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }
    }

    public void toggleLike(Long postId) {
        Long userId = UserContext.getUserId();
        String key = likeKey(postId);
        String uid = String.valueOf(userId);
        Boolean member = redisTemplate.opsForSet().isMember(key, uid);
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        if (Boolean.TRUE.equals(member)) {
            redisTemplate.opsForSet().remove(key, uid);
            int lc = post.getLikeCount() != null ? post.getLikeCount() : 0;
            post.setLikeCount(Math.max(0, lc - 1));
        } else {
            redisTemplate.opsForSet().add(key, uid);
            int lc = post.getLikeCount() != null ? post.getLikeCount() : 0;
            post.setLikeCount(lc + 1);
        }
        postMapper.updateById(post);
    }

    public PageResult<Map<String, Object>> listPending(int page, int size) {
        return listForAdmin("pending", page, size);
    }

    public PageResult<Map<String, Object>> listForAdmin(String status, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreateTime);
        if (StringUtils.hasText(status) && !"all".equals(status)) {
            wrapper.eq(Post::getAuditStatus, status);
        }
        long total = postMapper.selectCount(wrapper);
        List<Post> list = postMapper.selectList(wrapper.last("LIMIT " + (page - 1) * size + "," + size));
        List<Map<String, Object>> records = list.stream().map(p -> {
            Map<String, Object> m = toMap(p);
            m.put("sensitiveHit", sensitiveWordService.findHit(p.getTitle() + p.getContent()));
            return m;
        }).collect(Collectors.toList());
        return new PageResult<>(records, total, page, size);
    }

    public void approvePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setAuditStatus("approved");
            postMapper.updateById(post);
            notifyPostAudit(post, true, null);
        }
    }

    public void approvePosts(List<Long> postIds) {
        for (Long postId : postIds) {
            approvePost(postId);
        }
    }

    public void rejectPost(Long postId, String reason) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setAuditStatus("rejected");
            post.setRejectReason(reason);
            postMapper.updateById(post);
            notifyPostAudit(post, false, reason);
        }
    }

    public void rejectPosts(List<Long> postIds, String reason) {
        for (Long postId : postIds) {
            rejectPost(postId, reason);
        }
    }

    public void deletePost(Long postId) {
        commentMapper.delete(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId));
        postMapper.deleteById(postId);
        redisTemplate.delete(likeKey(postId));
    }

    public void deletePosts(List<Long> postIds) {
        for (Long postId : postIds) {
            deletePost(postId);
        }
    }

    private void notifyPostAudit(Post post, boolean passed, String reason) {
        if (post.getUserId() == null) return;
        if (passed) {
            messageService.sendMessage(post.getUserId(), "帖子审核通过",
                    "您的帖子「" + post.getTitle() + "」已通过审核，现已在论坛展示。", "post_approve",
                    "post", post.getPostId(), null);
        } else {
            String detail = StringUtils.hasText(reason) ? reason : "请前往论坛「我的帖子」修改后重新提交";
            messageService.sendMessage(post.getUserId(), "帖子审核未通过",
                    "您的帖子「" + post.getTitle() + "」未通过：" + detail, "post_reject",
                    "post", post.getPostId(), null);
        }
    }

    private String likeKey(Long postId) {
        return "post:like:" + postId;
    }

    private Map<String, Object> toMap(Post p) {
        Map<String, Object> m = new HashMap<>();
        m.put("postId", p.getPostId());
        m.put("title", p.getTitle());
        m.put("content", p.getContent());
        if (StringUtils.hasText(p.getImages())) {
            m.put("images", com.alibaba.fastjson.JSON.parseArray(p.getImages(), String.class));
        } else {
            m.put("images", new ArrayList<>());
        }
        m.put("zone", p.getZone());
        m.put("likeCount", p.getLikeCount());
        m.put("commentCount", p.getCommentCount());
        m.put("auditStatus", p.getAuditStatus());
        m.put("rejectReason", p.getRejectReason());
        m.put("createTime", p.getCreateTime());
        return m;
    }
}
