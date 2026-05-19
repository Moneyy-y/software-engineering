package com.catering.dto;

import lombok.Data;

@Data
public class UserSaveDTO {
    private Long userId;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String mobile;
    private String role;
    private Integer status;
}