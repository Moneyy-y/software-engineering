package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.Report;
import com.catering.service.ReportService;
import com.catering.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @PostMapping("/submit")
    public Result<Void> submitReport(@RequestBody Map<String, Object> request) {
        String targetType = (String) request.get("targetType");
        Long targetId = ((Number) request.get("targetId")).longValue();
        String reason = (String) request.get("reason");
        String description = (String) request.get("description");
        
        reportService.submitReport(userService.getCurrentUserId(), targetType, targetId, reason, description);
        return Result.ok();
    }

    @GetMapping("/list")
    public Result<List<Report>> getReports(@RequestParam(defaultValue = "pending") String status) {
        return Result.ok(reportService.getReportsByStatus(status));
    }

    @GetMapping("/my")
    public Result<List<Report>> getMyReports() {
        return Result.ok(reportService.getReportsByUserId(userService.getCurrentUserId()));
    }

    @PutMapping("/handle/{id}")
    public Result<Void> handleReport(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String status = (String) request.get("status");
        String handleResult = (String) request.get("handleResult");
        
        reportService.handleReport(id, status, userService.getCurrentUserId(), handleResult);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return Result.ok();
    }
}