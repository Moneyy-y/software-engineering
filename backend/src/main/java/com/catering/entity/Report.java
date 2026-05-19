package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long reportId;
    private Long userId;
    private String targetType;
    private Long targetId;
    private String reason;
    private String description;
    private String status;
    private Long handlerId;
    private String handleResult;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}