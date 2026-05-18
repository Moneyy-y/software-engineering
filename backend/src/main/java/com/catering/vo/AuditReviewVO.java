package com.catering.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditReviewVO {
    private Long reviewId;
    private Long dishId;
    private String dishName;
    private Integer score;
    private String content;
    private String images;
    private String auditStatus;
    private String sensitiveHit;
    private LocalDateTime createTime;
}
