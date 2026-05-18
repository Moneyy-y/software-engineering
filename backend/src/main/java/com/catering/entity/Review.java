package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long reviewId;
    private String userId;
    private Long dishId;
    private Long shopId;
    private Integer score;
    private String content;
    private String images;
    private Integer isAnonymous;
    private String auditStatus;
    private String rejectReason;
    private Long auditorId;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}
