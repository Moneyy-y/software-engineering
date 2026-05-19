package com.catering.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.entity.Dish;
import com.catering.entity.Post;
import com.catering.entity.Review;
import com.catering.mapper.DishMapper;
import com.catering.mapper.PostMapper;
import com.catering.mapper.ReviewMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BoardCalculationTask {

    private final DishMapper dishMapper;
    private final ReviewMapper reviewMapper;
    private final PostMapper postMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String RED_BOARD_KEY = "board:red:dishes";
    private static final String BLACK_BOARD_KEY = "board:black:dishes";
    private static final String RECOMMEND_CACHE_KEY = "recommend:daily:";
    private static final String POST_LIKE_KEY_PREFIX = "post:like:";

    public BoardCalculationTask(DishMapper dishMapper, ReviewMapper reviewMapper,
                                 PostMapper postMapper,
                                 StringRedisTemplate redisTemplate) {
        this.dishMapper = dishMapper;
        this.reviewMapper = reviewMapper;
        this.postMapper = postMapper;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void calculateRedBlackBoard() {
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1));

        redisTemplate.delete(RED_BOARD_KEY);
        redisTemplate.delete(BLACK_BOARD_KEY);

        for (Dish dish : dishes) {
            String boardStatus = dish.getBoardStatus();
            if ("red".equals(boardStatus)) {
                redisTemplate.opsForZSet().add(RED_BOARD_KEY, String.valueOf(dish.getDishId()),
                        dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0);
            } else if ("black".equals(boardStatus)) {
                redisTemplate.opsForZSet().add(BLACK_BOARD_KEY, String.valueOf(dish.getDishId()),
                        dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0);
            }
        }

        List<Dish> autoRed = dishes.stream()
                .filter(d -> d.getAvgScore() != null && d.getAvgScore().compareTo(new BigDecimal("4.5")) >= 0
                        && d.getSaleCount() != null && d.getSaleCount() > 100
                        && d.getBoardStatus() == null)
                .sorted((a, b) -> b.getAvgScore().compareTo(a.getAvgScore()))
                .limit(10)
                .collect(Collectors.toList());

        for (Dish dish : autoRed) {
            Long rank = redisTemplate.opsForZSet().rank(RED_BOARD_KEY, String.valueOf(dish.getDishId()));
            if (rank == null || rank >= 10) {
                redisTemplate.opsForZSet().add(RED_BOARD_KEY, String.valueOf(dish.getDishId()),
                        dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0);
            }
        }

        List<Dish> autoBlack = dishes.stream()
                .filter(d -> d.getAvgScore() != null && d.getAvgScore().compareTo(new BigDecimal("2.5")) < 0
                        && d.getReviewCount() != null && d.getReviewCount() >= 5
                        && d.getBoardStatus() == null)
                .sorted((a, b) -> a.getAvgScore().compareTo(b.getAvgScore()))
                .limit(10)
                .collect(Collectors.toList());

        for (Dish dish : autoBlack) {
            Long rank = redisTemplate.opsForZSet().rank(BLACK_BOARD_KEY, String.valueOf(dish.getDishId()));
            if (rank == null || rank >= 10) {
                redisTemplate.opsForZSet().add(BLACK_BOARD_KEY, String.valueOf(dish.getDishId()),
                        dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0);
            }
        }

        Set<String> redDishes = redisTemplate.opsForZSet().reverseRange(RED_BOARD_KEY, 0, 9);
        if (redDishes != null && !redDishes.isEmpty()) {
            for (String dishId : redDishes) {
                Dish dish = dishMapper.selectById(Long.valueOf(dishId));
                if (dish != null && dish.getBoardStatus() == null) {
                    dish.setBoardStatus("red");
                    dishMapper.updateById(dish);
                }
            }
        }

        Set<String> blackDishes = redisTemplate.opsForZSet().reverseRange(BLACK_BOARD_KEY, 0, 9);
        if (blackDishes != null && !blackDishes.isEmpty()) {
            for (String dishId : blackDishes) {
                Dish dish = dishMapper.selectById(Long.valueOf(dishId));
                if (dish != null && dish.getBoardStatus() == null) {
                    dish.setBoardStatus("black");
                    dishMapper.updateById(dish);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void updateDishStatistics() {
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1));

        for (Dish dish : dishes) {
            List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                    .eq(Review::getDishId, dish.getDishId())
                    .eq(Review::getAuditStatus, "approved"));

            if (reviews.isEmpty()) {
                dish.setAvgScore(BigDecimal.ZERO);
                dish.setReviewCount(0);
            } else {
                double avg = reviews.stream()
                        .mapToInt(Review::getScore)
                        .average()
                        .orElse(0);
                dish.setAvgScore(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
                dish.setReviewCount(reviews.size());
            }
            dishMapper.updateById(dish);
        }
    }

    @Scheduled(cron = "0 30 4 * * ?")
    public void refreshRecommendCache() {
        Set<String> keys = redisTemplate.keys(RECOMMEND_CACHE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void clearExpiredData() {
        Set<String> behaviorKeys = redisTemplate.keys("behavior:*");
        if (behaviorKeys != null && !behaviorKeys.isEmpty()) {
            redisTemplate.delete(behaviorKeys);
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void syncPostLikesFromRedisToDB() {
        Set<String> likeKeys = redisTemplate.keys(POST_LIKE_KEY_PREFIX + "*");
        if (likeKeys == null || likeKeys.isEmpty()) {
            return;
        }
        
        for (String key : likeKeys) {
            try {
                String postIdStr = key.substring(POST_LIKE_KEY_PREFIX.length());
                Long postId = Long.valueOf(postIdStr);
                
                Long likeCount = redisTemplate.opsForSet().size(key);
                if (likeCount != null && likeCount > 0) {
                    Post post = postMapper.selectById(postId);
                    if (post != null) {
                        post.setLikeCount(likeCount.intValue());
                        postMapper.updateById(post);
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}