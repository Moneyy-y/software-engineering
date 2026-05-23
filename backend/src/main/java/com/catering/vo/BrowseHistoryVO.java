package com.catering.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BrowseHistoryVO {
    private Long browseId;
    private Long dishId;
    private String dishName;
    private String coverImage;
    private BigDecimal price;
    private LocalDateTime createTime;
}
