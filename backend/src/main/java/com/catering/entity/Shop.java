package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop")
public class Shop {
    @TableId(type = IdType.AUTO)
    private Long shopId;
    private String name;
    private Integer type;
    private String logo;
    private String address;
    private BigDecimal lng;
    private BigDecimal lat;
    private String phone;
    private String businessHours;
    private BigDecimal avgPrice;
    private BigDecimal avgScore;
    private Integer status;
    private LocalDateTime createTime;
}
