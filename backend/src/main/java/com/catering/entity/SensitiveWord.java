package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sensitive_word")
public class SensitiveWord {
    @TableId(type = IdType.AUTO)
    private Long wordId;
    private String content;
    private String category;
    private Integer status;
}
