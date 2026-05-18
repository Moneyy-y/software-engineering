package com.catering.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishVO {
    private Long dishId;
    private String name;
    private BigDecimal price;
    private String coverImage;
    private String description;
    private String category;
    private BigDecimal avgScore;
    private Integer reviewCount;
    private Integer saleCount;
    private String shopName;
    private String stallName;
    private Long shopId;
    private Double distanceKm;
    private Boolean favorited;
}
