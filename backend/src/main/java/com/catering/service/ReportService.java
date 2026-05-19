package com.catering.service;

import com.catering.entity.Report;
import com.catering.mapper.ReportMapper;
import org.springframework.stereotype.Service;

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
        return reportMapper.selectByStatus(status);
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