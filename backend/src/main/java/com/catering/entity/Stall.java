package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("stall")
public class Stall {
    @TableId(type = IdType.AUTO)
    private Long stallId;
    private Long shopId;
    private String name;
    private String category;
    private Integer status;
    private Integer sortOrder;
}
