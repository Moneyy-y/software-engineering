package com.catering.vo;

import com.catering.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String mobile;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static UserVO of(User user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setMobile(user.getMobile());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}