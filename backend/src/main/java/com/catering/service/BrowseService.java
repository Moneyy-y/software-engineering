package com.catering.service;

import com.catering.entity.UserBrowse;
import com.catering.mapper.UserBrowseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrowseService {

    private final UserBrowseMapper browseMapper;

    public void addBrowse(Long userId, Long dishId, Long postId) {
        UserBrowse browse = new UserBrowse();
        browse.setUserId(userId);
        browse.setDishId(dishId);
        browse.setPostId(postId);
        browse.setCreateTime(LocalDateTime.now());
        browseMapper.insert(browse);
    }

    public List<UserBrowse> getBrowseHistory(Long userId) {
        return browseMapper.selectByUserIdOrderByTime(userId);
    }

    public void clearBrowseHistory(Long userId) {
        browseMapper.deleteByUserId(userId);
    }
}