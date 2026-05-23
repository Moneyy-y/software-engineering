package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.entity.Dish;
import com.catering.entity.UserBrowse;
import com.catering.mapper.DishMapper;
import com.catering.mapper.UserBrowseMapper;
import com.catering.vo.BrowseHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BrowseService {

    private final UserBrowseMapper browseMapper;
    private final DishMapper dishMapper;

    public void addBrowse(Long userId, Long dishId, Long postId) {
        if (dishId != null) {
            UserBrowse existing = browseMapper.selectOne(new LambdaQueryWrapper<UserBrowse>()
                    .eq(UserBrowse::getUserId, userId)
                    .eq(UserBrowse::getDishId, dishId)
                    .orderByDesc(UserBrowse::getCreateTime)
                    .last("LIMIT 1"));
            if (existing != null) {
                existing.setCreateTime(LocalDateTime.now());
                if (postId != null) {
                    existing.setPostId(postId);
                }
                browseMapper.updateById(existing);
                return;
            }
        }
        UserBrowse browse = new UserBrowse();
        browse.setUserId(userId);
        browse.setDishId(dishId);
        browse.setPostId(postId);
        browse.setCreateTime(LocalDateTime.now());
        browseMapper.insert(browse);
    }

    public List<BrowseHistoryVO> getBrowseHistory(Long userId) {
        List<UserBrowse> rows = browseMapper.selectByUserIdOrderByTime(userId);
        List<BrowseHistoryVO> result = new ArrayList<>();
        Set<Long> seenDishIds = new HashSet<>();
        for (UserBrowse row : rows) {
            if (row.getDishId() == null) continue;
            if (!seenDishIds.add(row.getDishId())) continue;
            Dish dish = dishMapper.selectById(row.getDishId());
            if (dish == null || dish.getStatus() != 1) continue;
            BrowseHistoryVO vo = new BrowseHistoryVO();
            vo.setBrowseId(row.getBrowseId());
            vo.setDishId(row.getDishId());
            vo.setDishName(dish.getName());
            vo.setCoverImage(dish.getCoverImage());
            vo.setPrice(dish.getPrice());
            vo.setCreateTime(row.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    public void clearBrowseHistory(Long userId) {
        browseMapper.deleteByUserId(userId);
    }
}
