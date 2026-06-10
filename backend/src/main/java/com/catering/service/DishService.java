package com.catering.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catering.common.BusinessException;
import com.catering.common.PageResult;
import com.catering.context.UserContext;
import com.catering.entity.*;
import com.catering.mapper.*;
import com.catering.util.LocationUtil;
import com.catering.vo.DishDetailVO;
import com.catering.vo.DishVO;
import com.catering.vo.ReviewVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DishService {

    private final DishMapper dishMapper;
    private final StallMapper stallMapper;
    private final ShopMapper shopMapper;
    private final ReviewMapper reviewMapper;
    private final UserFavoriteMapper favoriteMapper;

    public DishService(DishMapper dishMapper, StallMapper stallMapper, ShopMapper shopMapper,
                       ReviewMapper reviewMapper, UserFavoriteMapper favoriteMapper) {
        this.dishMapper = dishMapper;
        this.stallMapper = stallMapper;
        this.shopMapper = shopMapper;
        this.reviewMapper = reviewMapper;
        this.favoriteMapper = favoriteMapper;
    }

    public PageResult<DishVO> listDishes(String keyword, Long shopId, String category,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         String sortBy, Double lat, Double lng,
                                         int page, int size) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Dish::getName, keyword);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Dish::getCategory, category);
        }
        if (minPrice != null) wrapper.ge(Dish::getPrice, minPrice);
        if (maxPrice != null) wrapper.le(Dish::getPrice, maxPrice);

        List<Dish> all = dishMapper.selectList(wrapper);
        List<DishVO> vos = new ArrayList<>();
        Long userId = UserContext.getUserId();

        for (Dish dish : all) {
            Stall stall = stallMapper.selectById(dish.getStallId());
            if (stall == null) continue;
            if (shopId != null && !shopId.equals(stall.getShopId())) continue;
            Shop shop = shopMapper.selectById(stall.getShopId());
            if (shop == null || shop.getStatus() != 1) continue;

            DishVO vo = toDishVO(dish, stall, shop, lat, lng, userId);
            vos.add(vo);
        }

        if ("distance".equals(sortBy) && lat != null && lng != null) {
            vos.sort(Comparator.comparing(d -> d.getDistanceKm() != null ? d.getDistanceKm() : 999));
        } else if ("score".equals(sortBy)) {
            vos.sort((a, b) -> b.getAvgScore().compareTo(a.getAvgScore()));
        } else if ("sale".equals(sortBy)) {
            vos.sort((a, b) -> b.getSaleCount() - a.getSaleCount());
        } else {
            vos.sort((a, b) -> {
                double sa = score(a);
                double sb = score(b);
                return Double.compare(sb, sa);
            });
        }

        int from = Math.min((page - 1) * size, vos.size());
        int to = Math.min(from + size, vos.size());
        return new PageResult<>(vos.subList(from, to), vos.size(), page, size);
    }

    private double score(DishVO vo) {
        double s = vo.getAvgScore() != null ? vo.getAvgScore().doubleValue() : 0;
        double sale = vo.getSaleCount() != null ? Math.min(vo.getSaleCount() / 500.0, 1) : 0;
        double dist = vo.getDistanceKm() != null ? Math.max(0, 1 - vo.getDistanceKm() / 5.0) : 0;
        return s * 0.4 + sale * 0.3 + dist * 0.3;
    }

    public DishDetailVO getDetail(Long dishId, Double lat, Double lng) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || dish.getStatus() != 1) throw new BusinessException("菜品不存在或已下架");
        Stall stall = stallMapper.selectById(dish.getStallId());
        Shop shop = shopMapper.selectById(stall.getShopId());

        DishDetailVO vo = new DishDetailVO();
        vo.setDishId(dish.getDishId());
        vo.setName(dish.getName());
        vo.setPrice(dish.getPrice());
        vo.setOriginalPrice(dish.getOriginalPrice());
        vo.setCoverImage(dish.getCoverImage());
        if (StringUtils.hasText(dish.getImages())) {
            vo.setImages(JSON.parseArray(dish.getImages(), String.class));
        } else {
            vo.setImages(Collections.singletonList(dish.getCoverImage()));
        }
        vo.setDescription(dish.getDescription());
        vo.setCategory(dish.getCategory());
        vo.setAvgScore(dish.getAvgScore());
        vo.setReviewCount(dish.getReviewCount());
        vo.setSaleCount(dish.getSaleCount());
        vo.setShopName(shop.getName());
        vo.setStallName(stall.getName());
        vo.setShopId(shop.getShopId());
        if (lat != null && lng != null && shop.getLat() != null && shop.getLng() != null) {
            vo.setDistanceKm(LocationUtil.distanceKm(lat, lng,
                    shop.getLat().doubleValue(), shop.getLng().doubleValue()));
        }
        Long userId = UserContext.getUserId();
        if (userId != null) {
            vo.setFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                    .eq(UserFavorite::getUserId, userId).eq(UserFavorite::getDishId, dishId)) > 0);
        }
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getDishId, dishId).eq(Review::getAuditStatus, "approved")
                .orderByDesc(Review::getCreateTime).last("LIMIT 20"));
        vo.setReviews(reviews.stream().map(this::toReviewVO).collect(Collectors.toList()));
        return vo;
    }

    public List<Shop> listShops() {
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1)
                .orderByAsc(Shop::getShopId));
    }

    public List<Shop> listAllShops() {
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>().orderByAsc(Shop::getShopId));
    }

    public Dish saveDish(Dish dish) {
        if (dish.getDishId() == null) {
            dishMapper.insert(dish);
        } else {
            dishMapper.updateById(dish);
        }
        return dish;
    }

    public void deleteDish(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            dish.setStatus(0);
            dishMapper.updateById(dish);
        }
    }

    public List<Stall> listStalls(Long shopId) {
        return stallMapper.selectList(new LambdaQueryWrapper<Stall>()
                .eq(Stall::getShopId, shopId)
                .orderByAsc(Stall::getSortOrder)
                .orderByAsc(Stall::getStallId));
    }

    public Shop saveShop(Shop shop) {
        if (shop.getShopId() != null && Integer.valueOf(0).equals(shop.getStatus())) {
            deleteShop(shop.getShopId());
            Shop deleted = shopMapper.selectById(shop.getShopId());
            return deleted != null ? deleted : shop;
        }
        if (!StringUtils.hasText(shop.getName())) {
            throw new BusinessException(1001, "名称不能为空");
        }
        if (shop.getType() == null) {
            throw new BusinessException(1001, "类型不能为空");
        }
        String name = shop.getName().trim();
        shop.setName(name);

        LambdaQueryWrapper<Shop> dup = new LambdaQueryWrapper<Shop>()
                .eq(Shop::getName, name)
                .eq(Shop::getStatus, 1)
                .ne(shop.getShopId() != null, Shop::getShopId, shop.getShopId());
        if (shopMapper.selectCount(dup) > 0) {
            throw new BusinessException(1005, "食堂名称已存在");
        }

        if (shop.getStatus() == null) {
            shop.setStatus(1);
        }
        if (shop.getShopId() == null) {
            shopMapper.insert(shop);
        } else {
            shopMapper.updateById(shop);
        }
        return shop;
    }

    public void deleteShop(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(1004, "食堂不存在");
        }
        List<Stall> stalls = stallMapper.selectList(new LambdaQueryWrapper<Stall>()
                .eq(Stall::getShopId, shopId)
                .eq(Stall::getStatus, 1));
        for (Stall stall : stalls) {
            ensureStallHasNoActiveDish(stall.getStallId());
        }
        shop.setStatus(0);
        shopMapper.updateById(shop);
        for (Stall stall : stalls) {
            stall.setStatus(0);
            stallMapper.updateById(stall);
        }
    }

    public Stall saveStall(Stall stall) {
        if (stall.getStallId() != null && Integer.valueOf(0).equals(stall.getStatus())) {
            deleteStall(stall.getStallId());
            Stall deleted = stallMapper.selectById(stall.getStallId());
            return deleted != null ? deleted : stall;
        }
        if (stall.getShopId() == null) {
            throw new BusinessException(1001, "所属食堂不能为空");
        }
        if (!StringUtils.hasText(stall.getName())) {
            throw new BusinessException(1001, "档口名称不能为空");
        }
        String name = stall.getName().trim();
        stall.setName(name);
        if (stall.getCategory() != null) {
            stall.setCategory(stall.getCategory().trim());
        }

        LambdaQueryWrapper<Stall> dup = new LambdaQueryWrapper<Stall>()
                .eq(Stall::getShopId, stall.getShopId())
                .eq(Stall::getName, name)
                .eq(Stall::getStatus, 1)
                .ne(stall.getStallId() != null, Stall::getStallId, stall.getStallId());
        if (stallMapper.selectCount(dup) > 0) {
            throw new BusinessException(1005, "该食堂下档口名称已存在");
        }

        if (stall.getStatus() == null) {
            stall.setStatus(1);
        }
        if (stall.getStallId() == null) {
            stallMapper.insert(stall);
        } else {
            stallMapper.updateById(stall);
        }
        return stall;
    }

    public void deleteStall(Long stallId) {
        Stall stall = stallMapper.selectById(stallId);
        if (stall == null) {
            throw new BusinessException(1004, "档口不存在");
        }
        ensureStallHasNoActiveDish(stallId);
        stall.setStatus(0);
        stallMapper.updateById(stall);
    }

    private void ensureStallHasNoActiveDish(Long stallId) {
        Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStallId, stallId)
                .eq(Dish::getStatus, 1));
        if (count != null && count > 0) {
            throw new BusinessException(1006, "档口下存在上架菜品，请先下架菜品");
        }
    }

    private DishVO toDishVO(Dish dish, Stall stall, Shop shop, Double lat, Double lng, Long userId) {
        DishVO vo = new DishVO();
        vo.setDishId(dish.getDishId());
        vo.setName(dish.getName());
        vo.setPrice(dish.getPrice());
        vo.setCoverImage(dish.getCoverImage());
        vo.setDescription(dish.getDescription());
        vo.setCategory(dish.getCategory());
        vo.setAvgScore(dish.getAvgScore());
        vo.setReviewCount(dish.getReviewCount());
        vo.setSaleCount(dish.getSaleCount());
        vo.setShopName(shop.getName());
        vo.setStallName(stall.getName());
        vo.setShopId(shop.getShopId());
        if (lat != null && lng != null && shop.getLat() != null && shop.getLng() != null) {
            vo.setDistanceKm(LocationUtil.distanceKm(lat, lng,
                    shop.getLat().doubleValue(), shop.getLng().doubleValue()));
        }
        if (userId != null) {
            vo.setFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                    .eq(UserFavorite::getUserId, userId).eq(UserFavorite::getDishId, dish.getDishId())) > 0);
        }
        return vo;
    }

    private ReviewVO toReviewVO(Review r) {
        ReviewVO vo = new ReviewVO();
        vo.setReviewId(r.getReviewId());
        vo.setScore(r.getScore());
        vo.setContent(r.getContent());
        vo.setImages(r.getImages());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }
}
