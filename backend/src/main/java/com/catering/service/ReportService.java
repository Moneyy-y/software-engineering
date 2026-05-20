package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.entity.Report;
import com.catering.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class ReportService {
    private final ReportMapper reportMapper;

    public ReportService(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    public void submitReport(Long userId, String targetType, Long targetId, String reason, String description) {
        Report report = new Report();
        report.setUserId(userId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("pending");
        reportMapper.insert(report);
    }

    public List<Report> getReportsByStatus(String status) {
        return listReports(status);
    }

    public List<Report> listReports(String status) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>()
                .orderByDesc(Report::getCreateTime);
        if (!StringUtils.hasText(status) || "all".equals(status)) {
            return reportMapper.selectList(wrapper);
        }
        if ("handled".equals(status)) {
            wrapper.in(Report::getStatus, Arrays.asList("approved", "rejected"));
            return reportMapper.selectList(wrapper);
        }
        wrapper.eq(Report::getStatus, status);
        return reportMapper.selectList(wrapper);
    }

    public List<Report> getReportsByUserId(Long userId) {
        return reportMapper.selectByUserId(userId);
    }

    public void handleReport(Long reportId, String status, Long handlerId, String handleResult) {
        reportMapper.handleReport(reportId, status, handlerId, handleResult);
    }

    public void deleteReport(Long reportId) {
        reportMapper.deleteById(reportId);
    }
}