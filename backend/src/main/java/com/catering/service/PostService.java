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

    public PostService(PostMapper postMapper, CommentMapper commentMapper, DishMapper dishMapper,
                       SensitiveWordService sensitiveWordService,
                       StringRedisTemplate redisTemplate) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.dishMapper = dishMapper;
        this.sensitiveWordService = sensitiveWordService;
        this.redisTemplate = redisTemplate;
    }

    public void publish(String title, String content, String zone) {
        String hit = sensitiveWordService.findHit(title + content);
        if (hit != null) throw new BusinessException(2001, "内容包含敏感词: " + hit);
        Post post = new Post();
        post.setUserId(UserContext.getUserId());
        post.setTitle(title);
        post.setContent(content);
        post.setZone(zone != null ? zone : "general");
        post.setAuditStatus("pending");
        postMapper.insert(post);
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
        return commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId).orderByDesc(Comment::getCreateTime))
                .stream().map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("commentId", c.getCommentId());
                    m.put("content", c.getContent());
                    m.put("createTime", c.getCreateTime());
                    return m;
                }).collect(Collectors.toList());
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
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getAuditStatus, "pending")
                .orderByDesc(Post::getCreateTime);
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

    public Map<String, Object> getBoardList(int page, int size) {
        List<Dish> allDishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1));
        List<Map<String, Object>> redList = new ArrayList<>();
        List<Map<String, Object>> blackList = new ArrayList<>();
        for (Dish dish : allDishes) {
            Map<String, Object> item = new HashMap<>();
            item.put("dishId", dish.getDishId());
            item.put("name", dish.getName());
            item.put("avgScore", dish.getAvgScore());
            item.put("saleCount", dish.getSaleCount());
            item.put("boardStatus", dish.getBoardStatus());
            if ("red".equals(dish.getBoardStatus())) {
                redList.add(item);
            } else if ("black".equals(dish.getBoardStatus())) {
                blackList.add(item);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("redList", redList);
        result.put("blackList", blackList);
        return result;
    }

    public void interveneBoard(Long dishId, String action) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        switch (action) {
            case "add_red":
                dish.setBoardStatus("red");
                break;
            case "add_black":
                dish.setBoardStatus("black");
                break;
            case "remove_red":
                dish.setBoardStatus("red_remove");
                break;
            case "remove_black":
                dish.setBoardStatus("black_remove");
                break;
            case "cancel":
                dish.setBoardStatus(null);
                break;
            default:
                throw new BusinessException("无效的操作");
        }
        dishMapper.updateById(dish);
    }

    public void batchInterveneBoard(List<Long> dishIds, String action) {
        for (Long dishId : dishIds) {
            interveneBoard(dishId, action);
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
        m.put("zone", p.getZone());
        m.put("likeCount", p.getLikeCount());
        m.put("commentCount", p.getCommentCount());
        m.put("auditStatus", p.getAuditStatus());
        m.put("createTime", p.getCreateTime());
        return m;
    }
}
