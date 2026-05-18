package com.catering.dto;

import lombok.Data;

@Data
public class AuditRejectDTO {
    private Long reviewId;
    private String reason;
}
