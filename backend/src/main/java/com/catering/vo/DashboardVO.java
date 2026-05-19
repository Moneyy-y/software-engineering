package com.catering.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private long todayReviewCount;
    private long pendingAuditCount;
    private long pendingReviewCount;
    private long pendingPostCount;
    private long pendingFeedbackCount;
    private long totalDishCount;
    private List<Map<String, Object>> scoreTrendData;
    private List<Map<String, Object>> complaintDistData;
    private List<Map<String, Object>> hotDishTop10;
}
