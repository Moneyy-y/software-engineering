package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.context.UserContext;
import com.catering.entity.*;
import com.catering.mapper.*;
import com.catering.util.LocationUtil;
import com.catering.vo.DishVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {

    private final DishMapper dishMapper;
    private final StallMapper stallMapper;
    private final ShopMapper shopMapper;
    private final UserBehaviorMapper behaviorMapper;
    private final ReviewMapper reviewMapper;

    public RecommendService(DishMapper dishMapper, StallMapper stallMapper, ShopMapper shopMapper,
                            UserBehaviorMapper behaviorMapper, ReviewMapper reviewMapper) {
        this.dishMapper = dishMapper;
        this.stallMapper = stallMapper;
        this.shopMapper = shopMapper;
        this.behaviorMapper = behaviorMapper;
        this.reviewMapper = reviewMapper;
    }

    public List<DishVO> recommendList(Double lat, Double lng, int limit) {
        Long userId = UserContext.getUserId();
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 1));
        if (dishes.isEmpty()) return Collections.emptyList();

        List<UserBehavior> behaviors = behaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>().eq(UserBehavior::getUserId, userId));

        Map<Long, Double> scores = new HashMap<>();
        if (behaviors.size() >= 3) {
            Set<Long> preferredCategories = new HashSet<>();
            for (UserBehavior b : behaviors) {
                Dish d = dishMapper.selectById(b.getDishId());
                if (d != null) preferredCategories.add(d.getStallId());
            }
            for (Dish dish : dishes) {
                double score = dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0;
                if (preferredCategories.contains(dish.getStallId())) score += 1.0;
                double weight = behaviors.stream().filter(b -> b.getDishId().equals(dish.getDishId()))
                        .mapToDouble(b -> "review".equals(b.getActionType()) ? 3 : "favorite".equals(b.getActionType()) ? 2 : 1)
                        .sum();
                scores.put(dish.getDishId(), score + weight * 0.5);
            }
        } else {
            for (Dish dish : dishes) {
                double hot = (dish.getSaleCount() != null ? dish.getSaleCount() : 0) * 0.6
                        + (dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0) * 0.4;
                scores.put(dish.getDishId(), hot);
            }
        }

        List<DishVO> result = new ArrayList<>();
        dishes.stream()
                .sorted((a, b) -> Double.compare(scores.getOrDefault(b.getDishId(), 0.0),
                        scores.getOrDefault(a.getDishId(), 0.0)))
                .limit(limit)
                .forEach(dish -> {
                    Stall stall = stallMapper.selectById(dish.getStallId());
                    if (stall == null) return;
                    Shop shop = shopMapper.selectById(stall.getShopId());
                    if (shop == null) return;
                    DishVO vo = new DishVO();
                    vo.setDishId(dish.getDishId());
                    vo.setName(dish.getName());
                    vo.setPrice(dish.getPrice());
                    vo.setCoverImage(dish.getCoverImage());
                    vo.setAvgScore(dish.getAvgScore());
                    vo.setSaleCount(dish.getSaleCount());
                    vo.setShopName(shop.getName());
                    if (lat != null && lng != null && shop.getLat() != null && shop.getLng() != null) {
                        double dist = LocationUtil.distanceKm(lat, lng,
                                shop.getLat().doubleValue(), shop.getLng().doubleValue());
                        vo.setDistanceKm(dist);
                        if (dist <= 0.5) {
                            scores.merge(dish.getDishId(), scores.get(dish.getDishId()) * 1.5, (a, b) -> b);
                        }
                    }
                    result.add(vo);
                });
        return result;
    }

    public Map<String, List<DishVO>> redBlackBoard() {
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 1));
        List<DishVO> all = dishes.stream().map(this::toSimpleVO).collect(Collectors.toList());

        List<DishVO> red = all.stream()
                .filter(d -> d.getAvgScore() != null && d.getAvgScore().compareTo(new BigDecimal("4.5")) >= 0
                        && d.getSaleCount() != null && d.getSaleCount() > 100)
                .sorted((a, b) -> b.getAvgScore().compareTo(a.getAvgScore()))
                .limit(10).collect(Collectors.toList());

        List<DishVO> black = all.stream()
                .filter(d -> d.getAvgScore() != null && d.getAvgScore().compareTo(new BigDecimal("2.5")) < 0
                        && d.getReviewCount() != null && d.getReviewCount() >= 5)
                .sorted(Comparator.comparing(DishVO::getAvgScore))
                .limit(10).collect(Collectors.toList());

        Map<String, List<DishVO>> map = new HashMap<>();
        map.put("red", red);
        map.put("black", black);
        return map;
    }

    private DishVO toSimpleVO(Dish dish) {
        DishVO vo = new DishVO();
        vo.setDishId(dish.getDishId());
        vo.setName(dish.getName());
        vo.setPrice(dish.getPrice());
        vo.setCoverImage(dish.getCoverImage());
        vo.setAvgScore(dish.getAvgScore());
        vo.setSaleCount(dish.getSaleCount());
        vo.setReviewCount(dish.getReviewCount());
        return vo;
    }
}
