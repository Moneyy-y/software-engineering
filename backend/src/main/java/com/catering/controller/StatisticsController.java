package com.catering.controller;

import com.catering.common.Result;
import com.catering.service.StatisticsService;
import com.catering.vo.DashboardVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.ok(statisticsService.getDashboard());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        String csv = statisticsService.exportCsv();
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard-report.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }
}
