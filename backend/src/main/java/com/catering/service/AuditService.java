package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.BusinessException;
import com.catering.common.PageResult;
import com.catering.context.UserContext;
import com.catering.entity.Dish;
import com.catering.entity.Review;
import com.catering.mapper.DishMapper;
import com.catering.mapper.ReviewMapper;
import com.catering.vo.AuditReviewVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {

    private final ReviewMapper reviewMapper;
    private final DishMapper dishMapper;
    private final ReviewService reviewService;
    private final SensitiveWordService sensitiveWordService;
    private final MessageService messageService;
    private final com.catering.util.AesUtil aesUtil;

    public AuditService(ReviewMapper reviewMapper, DishMapper dishMapper,
                        ReviewService reviewService, SensitiveWordService sensitiveWordService,
                        MessageService messageService, com.catering.util.AesUtil aesUtil) {
        this.reviewMapper = reviewMapper;
        this.dishMapper = dishMapper;
        this.reviewService = reviewService;
        this.sensitiveWordService = sensitiveWordService;
        this.messageService = messageService;
        this.aesUtil = aesUtil;
    }

    public PageResult<AuditReviewVO> listPending(String status, int page, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status) && !"all".equals(status)) {
            wrapper.eq(Review::getAuditStatus, status);
        } else {
            wrapper.eq(Review::getAuditStatus, "pending");
        }
        wrapper.orderByDesc(Review::getCreateTime);
        long total = reviewMapper.selectCount(wrapper);
        List<Review> list = reviewMapper.selectList(wrapper.last("LIMIT " + (page - 1) * size + "," + size));

        List<AuditReviewVO> vos = new ArrayList<>();
        for (Review r : list) {
            AuditReviewVO vo = new AuditReviewVO();
            vo.setReviewId(r.getReviewId());
            vo.setDishId(r.getDishId());
            Dish dish = dishMapper.selectById(r.getDishId());
            vo.setDishName(dish != null ? dish.getName() : "");
            vo.setScore(r.getScore());
            vo.setContent(r.getContent());
            vo.setImages(r.getImages());
            vo.setAuditStatus(r.getAuditStatus());
            vo.setSensitiveHit(sensitiveWordService.findHit(r.getContent()));
            vo.setCreateTime(r.getCreateTime());
            vos.add(vo);
        }
        return new PageResult<>(vos, total, page, size);
    }

    public void passReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) throw new BusinessException("评价不存在");
        review.setAuditStatus("approved");
        review.setAuditorId(UserContext.getUserId());
        review.setAuditTime(LocalDateTime.now());
        reviewMapper.updateById(review);
        reviewService.updateDishAvgScore(review.getDishId());
        notifyReviewAudit(review, true, null);
    }

    public void rejectReview(Long reviewId, String reason) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) throw new BusinessException("评价不存在");
        review.setAuditStatus("rejected");
        review.setRejectReason(reason);
        review.setAuditorId(UserContext.getUserId());
        review.setAuditTime(LocalDateTime.now());
        reviewMapper.updateById(review);
        reviewService.clearReviewLimit(review.getUserId(), review.getDishId());
        notifyReviewAudit(review, false, reason);
    }

    private void notifyReviewAudit(Review review, boolean passed, String reason) {
        Long userId = aesUtil.decryptUserId(review.getUserId());
        if (userId == null) return;
        Dish dish = dishMapper.selectById(review.getDishId());
        String dishName = dish != null ? dish.getName() : "菜品";
        if (passed) {
            messageService.sendMessage(userId, "评价审核通过",
                    "您对「" + dishName + "」的评价已通过审核，现已公开展示。", "review_approve",
                    "review", review.getReviewId(), review.getDishId());
        } else {
            String detail = StringUtils.hasText(reason) ? reason : "请修改后重新提交";
            messageService.sendMessage(userId, "评价审核未通过",
                    "您对「" + dishName + "」的评价未通过：" + detail, "review_reject",
                    "review", review.getReviewId(), review.getDishId());
        }
    }

    public void batchPass(List<Long> reviewIds) {
        for (Long id : reviewIds) {
            passReview(id);
        }
    }
}
