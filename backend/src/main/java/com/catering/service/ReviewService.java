package com.catering.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.BusinessException;
import com.catering.common.PageResult;
import com.catering.context.UserContext;
import com.catering.dto.ReviewSubmitDTO;
import com.catering.entity.*;
import com.catering.mapper.*;
import com.catering.util.AesUtil;
import com.catering.vo.ReviewVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final DishMapper dishMapper;
    private final StallMapper stallMapper;
    private final UserBehaviorMapper behaviorMapper;
    private final SensitiveWordService sensitiveWordService;
    private final AesUtil aesUtil;
    private final StringRedisTemplate redisTemplate;

    public ReviewService(ReviewMapper reviewMapper, DishMapper dishMapper, StallMapper stallMapper,
                         UserBehaviorMapper behaviorMapper, SensitiveWordService sensitiveWordService,
                         AesUtil aesUtil, StringRedisTemplate redisTemplate) {
        this.reviewMapper = reviewMapper;
        this.dishMapper = dishMapper;
        this.stallMapper = stallMapper;
        this.behaviorMapper = behaviorMapper;
        this.sensitiveWordService = sensitiveWordService;
        this.aesUtil = aesUtil;
        this.redisTemplate = redisTemplate;
    }

    public void submitReview(ReviewSubmitDTO dto) {
        Long userId = UserContext.getUserId();
        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) {
            throw new BusinessException(1001, "评分需在1-5之间");
        }
        if (!StringUtils.hasText(dto.getContent()) || dto.getContent().length() < 10) {
            throw new BusinessException(1001, "评价内容至少10字");
        }
        String hit = sensitiveWordService.findHit(dto.getContent());
        if (hit != null) throw new BusinessException(2001, "评价内容包含敏感词: " + hit);

        String encUserId = aesUtil.encryptUserId(userId);
        String limitKeyEnc = "review:limit:" + encUserId + ":" + dto.getDishId();
        String limitKeyLegacy = "review:limit:" + userId + ":" + dto.getDishId();

        Dish dish = dishMapper.selectById(dto.getDishId());
        if (dish == null || dish.getStatus() != 1) throw new BusinessException("菜品不存在或已下架");
        Stall stall = stallMapper.selectById(dish.getStallId());

        if (Boolean.TRUE.equals(dto.getResubmit())) {
            if (!hasRejectedReview(userId, dto.getDishId())) {
                throw new BusinessException(2001, "无被拒评价，无法重新提交");
            }
            redisTemplate.delete(limitKeyEnc);
            redisTemplate.delete(limitKeyLegacy);
            Review rejected = findLatestRejectedReview(userId, dto.getDishId());
            if (rejected == null) {
                throw new BusinessException(2001, "无被拒评价，无法重新提交");
            }
            rejected.setScore(dto.getScore());
            rejected.setContent(dto.getContent());
            rejected.setImages(dto.getImages() != null ? JSON.toJSONString(dto.getImages()) : "[]");
            rejected.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 1);
            rejected.setAuditStatus("pending");
            rejected.setRejectReason(null);
            rejected.setAuditorId(null);
            rejected.setAuditTime(null);
            reviewMapper.updateById(rejected);
        } else {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKeyEnc))
                    || Boolean.TRUE.equals(redisTemplate.hasKey(limitKeyLegacy))) {
                throw new BusinessException(2001, "24小时内已对同一菜品评价");
            }
            Review review = new Review();
            review.setUserId(aesUtil.encryptUserId(userId));
            review.setDishId(dto.getDishId());
            review.setShopId(stall.getShopId());
            review.setScore(dto.getScore());
            review.setContent(dto.getContent());
            review.setImages(dto.getImages() != null ? JSON.toJSONString(dto.getImages()) : "[]");
            review.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 1);
            review.setAuditStatus("pending");
            reviewMapper.insert(review);
        }

        redisTemplate.opsForValue().set(limitKeyEnc, "1", 24, TimeUnit.HOURS);

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setDishId(dto.getDishId());
        behavior.setActionType("review");
        behaviorMapper.insert(behavior);
    }

    public PageResult<ReviewVO> listByDish(Long dishId, int page, int size) {
        PageResult<ReviewVO> result = new PageResult<>(null, 0, page, size);
        List<Review> list = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getDishId, dishId).eq(Review::getAuditStatus, "approved")
                .orderByDesc(Review::getCreateTime)
                .last("LIMIT " + (page - 1) * size + "," + size));
        long total = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getDishId, dishId).eq(Review::getAuditStatus, "approved"));
        List<ReviewVO> vos = list.stream().map(r -> {
            ReviewVO vo = new ReviewVO();
            vo.setReviewId(r.getReviewId());
            vo.setScore(r.getScore());
            vo.setContent(r.getContent());
            vo.setImages(r.getImages());
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(vos, total, page, size);
    }

    private boolean hasRejectedReview(Long userId, Long dishId) {
        return findLatestRejectedReview(userId, dishId) != null;
    }

    private Review findLatestRejectedReview(Long userId, Long dishId) {
        String enc = aesUtil.encryptUserId(userId);
        String legacy = aesUtil.legacyEncryptUserId(userId);
        return reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getDishId, dishId)
                .eq(Review::getAuditStatus, "rejected")
                .and(w -> w.eq(Review::getUserId, enc).or().eq(Review::getUserId, legacy))
                .orderByDesc(Review::getCreateTime)
                .last("LIMIT 1"));
    }

    public void clearReviewLimit(String storedUserId, Long dishId) {
        if (storedUserId != null) {
            redisTemplate.delete("review:limit:" + storedUserId + ":" + dishId);
        }
        Long uid = aesUtil.decryptUserId(storedUserId);
        if (uid != null) {
            redisTemplate.delete("review:limit:" + uid + ":" + dishId);
            redisTemplate.delete("review:limit:" + aesUtil.encryptUserId(uid) + ":" + dishId);
        }
    }

    @Async("taskExecutor")
    public void updateDishAvgScore(Long dishId) {
        List<Review> approved = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getDishId, dishId).eq(Review::getAuditStatus, "approved"));
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) return;
        if (approved.isEmpty()) {
            dish.setAvgScore(BigDecimal.ZERO);
            dish.setReviewCount(0);
        } else {
            double avg = approved.stream().mapToInt(Review::getScore).average().orElse(0);
            dish.setAvgScore(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
            dish.setReviewCount(approved.size());
        }
        dishMapper.updateById(dish);
    }
}
