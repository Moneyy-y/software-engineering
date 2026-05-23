package com.catering.vo;

import lombok.Data;

@Data
public class MessageVO {
    private Long messageId;
    private String title;
    private String content;
    private String type;
    private Boolean isRead;
    private String createTime;
    private String relatedType;
    private Long relatedId;
    private Long dishId;
}
