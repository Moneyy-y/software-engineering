package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.entity.Dish;
import com.catering.entity.Feedback;
import com.catering.entity.Post;
import com.catering.entity.Review;
import com.catering.mapper.DishMapper;
import com.catering.mapper.FeedbackMapper;
import com.catering.mapper.PostMapper;
import com.catering.mapper.ReviewMapper;
import com.catering.vo.DashboardVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final ReviewMapper reviewMapper;
    private final FeedbackMapper feedbackMapper;
    private final DishMapper dishMapper;
    private final PostMapper postMapper;

    public StatisticsService(ReviewMapper reviewMapper, FeedbackMapper feedbackMapper,
                            DishMapper dishMapper, PostMapper postMapper) {
        this.reviewMapper = reviewMapper;
        this.feedbackMapper = feedbackMapper;
        this.dishMapper = dishMapper;
        this.postMapper = postMapper;
    }

    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        vo.setTodayReviewCount(reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .ge(Review::getCreateTime, todayStart)));
        long pendingReviews = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getAuditStatus, "pending"));
        long pendingPosts = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getAuditStatus, "pending"));
        vo.setPendingAuditCount(pendingReviews + pendingPosts);
        vo.setPendingReviewCount(pendingReviews);
        vo.setPendingPostCount(pendingPosts);
        vo.setPendingFeedbackCount(feedbackMapper.selectCount(new LambdaQueryWrapper<Feedback>()
                .in(Feedback::getStatus, "pending", "processing")));
        vo.setTotalDishCount(dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 1)));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            List<Review> dayReviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                    .eq(Review::getAuditStatus, "approved")
                    .ge(Review::getCreateTime, start).lt(Review::getCreateTime, end));
            double avg = dayReviews.isEmpty() ? 0 :
                    dayReviews.stream().mapToInt(Review::getScore).average().orElse(0);
            Map<String, Object> m = new HashMap<>();
            m.put("date", day.toString());
            m.put("avgScore", Math.round(avg * 10) / 10.0);
            m.put("count", dayReviews.size());
            trend.add(m);
        }
        vo.setScoreTrendData(trend);

        List<Feedback> feedbacks = feedbackMapper.selectList(null);
        Map<String, Long> typeCount = feedbacks.stream()
                .collect(Collectors.groupingBy(f -> f.getType() != null ? f.getType() : "other", Collectors.counting()));
        List<Map<String, Object>> complaint = new ArrayList<>();
        typeCount.forEach((k, v) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("type", k);
            m.put("count", v);
            complaint.add(m);
        });
        vo.setComplaintDistData(complaint);

        List<Dish> hot = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1).orderByDesc(Dish::getSaleCount).last("LIMIT 10"));
        List<Map<String, Object>> hotList = hot.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", d.getName());
            m.put("saleCount", d.getSaleCount());
            m.put("avgScore", d.getAvgScore());
            return m;
        }).collect(Collectors.toList());
        vo.setHotDishTop10(hotList);
        return vo;
    }

    public String exportCsv() {
        DashboardVO vo = getDashboard();
        StringBuilder sb = new StringBuilder();
        sb.append("指标,数值\n");
        sb.append("今日评价数,").append(vo.getTodayReviewCount()).append("\n");
        sb.append("待审核评价,").append(vo.getPendingAuditCount()).append("\n");
        sb.append("待处理反馈,").append(vo.getPendingFeedbackCount()).append("\n");
        sb.append("菜品总数,").append(vo.getTotalDishCount()).append("\n");
        sb.append("\n日期,平均评分,评价数\n");
        for (Map<String, Object> row : vo.getScoreTrendData()) {
            sb.append(row.get("date")).append(",")
                    .append(row.get("avgScore")).append(",")
                    .append(row.get("count")).append("\n");
        }
        sb.append("\n菜品,销量,评分\n");
        for (Map<String, Object> row : vo.getHotDishTop10()) {
            sb.append(row.get("name")).append(",")
                    .append(row.get("saleCount")).append(",")
                    .append(row.get("avgScore")).append("\n");
        }
        return sb.toString();
    }
}
