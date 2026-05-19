package com.catering.dto;

import lombok.Data;

@Data
public class AdminLoginDTO {
    private String username;
    private String password;
    private String captcha;
    private String captchaKey;
}
