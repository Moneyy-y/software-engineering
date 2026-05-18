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

    public AuditService(ReviewMapper reviewMapper, DishMapper dishMapper,
                        ReviewService reviewService, SensitiveWordService sensitiveWordService) {
        this.reviewMapper = reviewMapper;
        this.dishMapper = dishMapper;
        this.reviewService = reviewService;
        this.sensitiveWordService = sensitiveWordService;
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
    }

    public void rejectReview(Long reviewId, String reason) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) throw new BusinessException("评价不存在");
        review.setAuditStatus("rejected");
        review.setRejectReason(reason);
        review.setAuditorId(UserContext.getUserId());
        review.setAuditTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    public void batchPass(List<Long> reviewIds) {
        for (Long id : reviewIds) {
            passReview(id);
        }
    }
}
