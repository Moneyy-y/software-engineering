package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.BusinessException;
import com.catering.common.PageResult;
import com.catering.context.UserContext;
import com.catering.dto.AdminLoginDTO;
import com.catering.dto.LoginDTO;
import com.catering.entity.Dish;
import com.catering.entity.Review;
import com.catering.entity.User;
import com.catering.entity.UserFavorite;
import com.catering.mapper.DishMapper;
import com.catering.mapper.ReviewMapper;
import com.catering.mapper.UserFavoriteMapper;
import com.catering.mapper.UserMapper;
import com.catering.util.AesUtil;
import com.catering.util.JwtUtil;
import com.catering.vo.ReviewVO;
import com.catering.vo.LoginVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final DishMapper dishMapper;
    private final ReviewMapper reviewMapper;
    private final AesUtil aesUtil;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper, UserFavoriteMapper favoriteMapper,
                       DishMapper dishMapper, ReviewMapper reviewMapper, AesUtil aesUtil,
                       JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.favoriteMapper = favoriteMapper;
        this.dishMapper = dishMapper;
        this.reviewMapper = reviewMapper;
        this.aesUtil = aesUtil;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    public LoginVO wechatLogin(LoginDTO dto) {
        String code = dto.getCode();
        if (code == null || code.isEmpty()) {
            throw new BusinessException(1001, "code不能为空");
        }
        String openId = "mock_" + code;
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname(dto.getNickname() != null ? dto.getNickname() : "微信用户");
            user.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : "");
            user.setRole("student");
            user.setStatus(1);
            userMapper.insert(user);
        }
        return buildLoginVO(user);
    }

    public LoginVO adminLogin(AdminLoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(1002, "用户名或密码错误");
        }
        if (!"admin".equals(user.getRole()) && !"auditor".equals(user.getRole())) {
            throw new BusinessException(1003, "无管理端权限");
        }
        return buildLoginVO(user);
    }

    public Map<String, Object> getUserInfo() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("role", user.getRole());
        return map;
    }

    public void addFavorite(Long dishId) {
        Long userId = UserContext.getUserId();
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId).eq(UserFavorite::getDishId, dishId));
        if (count > 0) return;
        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setDishId(dishId);
        favoriteMapper.insert(fav);
    }

    public void removeFavorite(Long dishId) {
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, UserContext.getUserId())
                .eq(UserFavorite::getDishId, dishId));
    }

    public PageResult<Map<String, Object>> listFavorites(int page, int size) {
        Long userId = UserContext.getUserId();
        List<UserFavorite> favs = favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime)
                .last("LIMIT " + (page - 1) * size + "," + size));
        long total = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId));
        List<Map<String, Object>> records = favs.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("dishId", f.getDishId());
            m.put("createTime", f.getCreateTime());
            Dish dish = dishMapper.selectById(f.getDishId());
            if (dish != null) {
                m.put("name", dish.getName());
                m.put("price", dish.getPrice());
                m.put("coverImage", dish.getCoverImage());
                m.put("avgScore", dish.getAvgScore());
            }
            return m;
        }).collect(Collectors.toList());
        return new PageResult<>(records, total, page, size);
    }

    public List<ReviewVO> listMyReviews() {
        Long userId = UserContext.getUserId();
        String enc = aesUtil.encryptUserId(userId);
        String legacy = aesUtil.legacyEncryptUserId(userId);
        List<Review> list = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .and(w -> w.eq(Review::getUserId, enc).or().eq(Review::getUserId, legacy))
                .orderByDesc(Review::getCreateTime));
        return list.stream().map(r -> {
            ReviewVO vo = new ReviewVO();
            vo.setReviewId(r.getReviewId());
            vo.setDishId(r.getDishId());
            vo.setScore(r.getScore());
            vo.setContent(r.getContent());
            vo.setImages(r.getImages());
            vo.setCreateTime(r.getCreateTime());
            vo.setAuditStatus(r.getAuditStatus());
            Dish dish = dishMapper.selectById(r.getDishId());
            if (dish != null) vo.setDishName(dish.getName());
            return vo;
        }).collect(Collectors.toList());
    }

    public void logout(String token) {
        redisTemplate.opsForValue().set("token:blacklist:" + token, "1", 2, TimeUnit.HOURS);
    }

    private LoginVO buildLoginVO(User user) {
        String token = jwtUtil.generateToken(user.getUserId(), user.getRole());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}
