package com.catering.controller;

import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.dto.AuditRejectDTO;
import com.catering.service.AuditService;
import com.catering.vo.AuditReviewVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/review/pending")
    public Result<PageResult<AuditReviewVO>> pending(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(auditService.listPending(status, page, size));
    }

    @PostMapping("/review/pass")
    public Result<Void> pass(@RequestBody Map<String, Long> body) {
        auditService.passReview(body.get("reviewId"));
        return Result.ok();
    }

    @PostMapping("/review/reject")
    public Result<Void> reject(@RequestBody AuditRejectDTO dto) {
        auditService.rejectReview(dto.getReviewId(), dto.getReason());
        return Result.ok();
    }

    @PostMapping("/review/batchPass")
    public Result<Void> batchPass(@RequestBody Map<String, List<Long>> body) {
        auditService.batchPass(body.get("reviewIds"));
        return Result.ok();
    }
}
