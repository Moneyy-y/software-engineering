package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_browse")
public class UserBrowse {
    @TableId(type = IdType.AUTO)
    private Long browseId;
    private Long userId;
    private Long dishId;
    private Long postId;
    private LocalDateTime createTime;
}