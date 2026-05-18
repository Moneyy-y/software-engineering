package com.catering.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishDetailVO {
    private Long dishId;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private List<String> images;
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
    private List<ReviewVO> reviews;
}
