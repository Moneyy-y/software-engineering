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
    private String boardStatus;
    /** 榜单内排序，数值越小越靠前 */
    private Integer boardSort;
    /** 1=在榜单中隐藏 */
    private Integer boardHidden;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
