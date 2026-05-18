package com.catering.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReviewSubmitDTO {
    private Long dishId;
    private Integer score;
    private String content;
    private List<String> images;
    private Integer isAnonymous;
}
