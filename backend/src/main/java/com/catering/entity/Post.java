package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private String images;
    private String zone;
    private Integer likeCount;
    private Integer commentCount;
    private String auditStatus;
    private String rejectReason;
    private LocalDateTime createTime;
}
