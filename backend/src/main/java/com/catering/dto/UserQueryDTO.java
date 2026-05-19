package com.catering.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String username;
    private String nickname;
    private String role;
    private Integer status;
}