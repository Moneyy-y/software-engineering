package com.catering.dto;

import lombok.Data;
import java.util.List;

@Data
public class FeedbackSubmitDTO {
    private String type;
    private String description;
    private List<String> images;
    /** 用户已确认重复提交时为 true */
    private Boolean confirmDuplicate;
}
