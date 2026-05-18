package com.catering.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long reviewId;
    private Integer score;
    private String content;
    private String images;
    private String userName = "匿名用户";
    private String userAvatar = "https://thirdwx.qlogo.cn/mmopen/vi_32/default.png";
    private LocalDateTime createTime;
    private String auditStatus;
    private Long dishId;
    private String dishName;
}
