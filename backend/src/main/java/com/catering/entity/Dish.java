package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long dishId;
    private Long stallId;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private String images;
    private String description;
    private String category;
    private String tags;
    private BigDecimal avgScore;
    private Integer reviewCount;
    private Integer saleCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
