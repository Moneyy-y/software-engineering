package com.catering.controller;

import com.catering.common.Result;
import com.catering.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String url = fileService.upload(file);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.ok(data);
    }
}
