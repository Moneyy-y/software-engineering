package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {
    @TableId(type = IdType.AUTO)
    private Long feedbackId;
    private Long userId;
    private String type;
    private String description;
    private String images;
    private String status;
    private Long handlerId;
    private String reply;
    private LocalDateTime acceptTime;
    private LocalDateTime resolveTime;
    private LocalDateTime createTime;
}
