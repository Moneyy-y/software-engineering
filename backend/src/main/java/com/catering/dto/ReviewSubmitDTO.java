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
    /** 被拒后重新提交时传 true，跳过 24 小时限评 */
    private Boolean resubmit;
}
