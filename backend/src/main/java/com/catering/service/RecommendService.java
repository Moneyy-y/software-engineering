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
    private final UserMapper userMapper;
    private final BoardService boardService;

    public RecommendService(DishMapper dishMapper, StallMapper stallMapper, ShopMapper shopMapper,
                            UserBehaviorMapper behaviorMapper, ReviewMapper reviewMapper,
                            UserMapper userMapper, BoardService boardService) {
        this.dishMapper = dishMapper;
        this.stallMapper = stallMapper;
        this.shopMapper = shopMapper;
        this.behaviorMapper = behaviorMapper;
        this.reviewMapper = reviewMapper;
        this.userMapper = userMapper;
        this.boardService = boardService;
    }

    public void addToRedList(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            dish.setBoardStatus("red");
            dishMapper.updateById(dish);
        }
    }

    public void addToBlackList(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            dish.setBoardStatus("black");
            dishMapper.updateById(dish);
        }
    }

    public void removeFromRedList(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            dish.setBoardStatus("red_remove");
            dishMapper.updateById(dish);
        }
    }

    public void removeFromBlackList(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            dish.setBoardStatus("black_remove");
            dishMapper.updateById(dish);
        }
    }

    public List<Long> getRedListDishes() {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getBoardStatus, "red"))
                .stream().map(Dish::getDishId).collect(Collectors.toList());
    }

    public List<Long> getBlackListDishes() {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getBoardStatus, "black"))
                .stream().map(Dish::getDishId).collect(Collectors.toList());
    }

    public List<DishVO> recommendList(Double lat, Double lng, int limit) {
        Long userId = UserContext.getUserId();
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 1));
        if (dishes.isEmpty()) return Collections.emptyList();

        List<UserBehavior> behaviors = behaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>().eq(UserBehavior::getUserId, userId));

        Map<Long, Double> scores = new HashMap<>();

        if (behaviors.size() >= 3) {
            scores = collaborativeFilteringRecommend(userId, dishes);
        } else {
            for (Dish dish : dishes) {
                double hot = (dish.getSaleCount() != null ? dish.getSaleCount() : 0) * 0.6
                        + (dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0) * 0.4;
                scores.put(dish.getDishId(), hot);
            }
        }

        final Map<Long, Double> finalScores = new HashMap<>(scores);
        int finalLimit = limit;
        
        List<DishVO> result = new ArrayList<>();
        dishes.stream()
                .sorted((a, b) -> Double.compare(finalScores.getOrDefault(b.getDishId(), 0.0),
                        finalScores.getOrDefault(a.getDishId(), 0.0)))
                .limit(finalLimit)
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
                    }
                    result.add(vo);
                });
        return result;
    }

    public Map<Long, Double> collaborativeFilteringRecommend(Long userId, List<Dish> allDishes) {
        List<UserBehavior> currentUserBehaviors = behaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>().eq(UserBehavior::getUserId, userId));
        if (currentUserBehaviors.isEmpty()) {
            Map<Long, Double> defaultScores = new HashMap<>();
            for (Dish dish : allDishes) {
                double hot = (dish.getSaleCount() != null ? dish.getSaleCount() : 0) * 0.6
                        + (dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0) * 0.4;
                defaultScores.put(dish.getDishId(), hot);
            }
            return defaultScores;
        }

        Set<Long> currentUserDishes = currentUserBehaviors.stream()
                .map(UserBehavior::getDishId).collect(Collectors.toSet());
        Map<Long, Integer> currentUserRatings = new HashMap<>();
        for (UserBehavior b : currentUserBehaviors) {
            int rating = "review".equals(b.getActionType()) ? 5 : "favorite".equals(b.getActionType()) ? 3 : 1;
            currentUserRatings.merge(b.getDishId(), rating, Integer::sum);
        }

        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>());
        Map<Long, Double> similarities = new HashMap<>();
        for (User otherUser : allUsers) {
            if (otherUser.getUserId().equals(userId)) continue;
            List<UserBehavior> otherBehaviors = behaviorMapper.selectList(
                    new LambdaQueryWrapper<UserBehavior>().eq(UserBehavior::getUserId, otherUser.getUserId()));
            if (otherBehaviors.isEmpty()) continue;
            Set<Long> otherDishes = otherBehaviors.stream()
                    .map(UserBehavior::getDishId).collect(Collectors.toSet());

            Set<Long> intersection = new HashSet<>(currentUserDishes);
            intersection.retainAll(otherDishes);
            if (intersection.isEmpty()) continue;

            Map<Long, Integer> otherRatings = new HashMap<>();
            for (UserBehavior b : otherBehaviors) {
                int rating = "review".equals(b.getActionType()) ? 5 : "favorite".equals(b.getActionType()) ? 3 : 1;
                otherRatings.merge(b.getDishId(), rating, Integer::sum);
            }

            double dotProduct = 0;
            for (Long dishId : intersection) {
                dotProduct += currentUserRatings.get(dishId) * otherRatings.get(dishId);
            }
            double norm1 = Math.sqrt(currentUserRatings.values().stream().mapToInt(i -> i * i).sum());
            double norm2 = Math.sqrt(otherRatings.values().stream().mapToInt(i -> i * i).sum());
            if (norm1 > 0 && norm2 > 0) {
                double similarity = dotProduct / (norm1 * norm2);
                if (similarity > 0) {
                    similarities.put(otherUser.getUserId(), similarity);
                }
            }
        }

        if (similarities.isEmpty()) {
            Map<Long, Double> defaultScores = new HashMap<>();
            for (Dish dish : allDishes) {
                double hot = (dish.getSaleCount() != null ? dish.getSaleCount() : 0) * 0.6
                        + (dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0) * 0.4;
                defaultScores.put(dish.getDishId(), hot);
            }
            return defaultScores;
        }

        Map<Long, Double> predictedScores = new HashMap<>();
        for (Dish dish : allDishes) {
            if (currentUserDishes.contains(dish.getDishId())) continue;
            double score = 0;
            double totalSimilarity = 0;
            for (Map.Entry<Long, Double> entry : similarities.entrySet()) {
                Long otherUserId = entry.getKey();
                Double similarity = entry.getValue();
                List<UserBehavior> otherBehaviors = behaviorMapper.selectList(
                        new LambdaQueryWrapper<UserBehavior>()
                                .eq(UserBehavior::getUserId, otherUserId)
                                .eq(UserBehavior::getDishId, dish.getDishId()));
                if (!otherBehaviors.isEmpty()) {
                    int rating = "review".equals(otherBehaviors.get(0).getActionType()) ? 5 :
                                 "favorite".equals(otherBehaviors.get(0).getActionType()) ? 3 : 1;
                    score += similarity * rating;
                    totalSimilarity += similarity;
                }
            }
            if (totalSimilarity > 0) {
                score = score / totalSimilarity;
                double baseScore = (dish.getSaleCount() != null ? dish.getSaleCount() : 0) * 0.3
                        + (dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0) * 0.7;
                predictedScores.put(dish.getDishId(), score * 0.7 + baseScore * 0.3);
            } else {
                double baseScore = (dish.getSaleCount() != null ? dish.getSaleCount() : 0) * 0.6
                        + (dish.getAvgScore() != null ? dish.getAvgScore().doubleValue() : 0) * 0.4;
                predictedScores.put(dish.getDishId(), baseScore);
            }
        }
        return predictedScores;
    }

    public Map<String, List<DishVO>> redBlackBoard() {
        return boardService.getPublicRedBlack();
    }
}
