package com.catering.dto;

import lombok.Data;
import java.util.List;

@Data
public class FeedbackSubmitDTO {
    private String type;
    private String description;
    private List<String> images;
}
