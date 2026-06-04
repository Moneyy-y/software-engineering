package com.catering.service;

import com.catering.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /** 允许上传的图片 MIME 类型 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "image/webp", "image/bmp", "image/svg+xml"
    ));

    /** 允许上传的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".svg"
    ));

    /** 图片最大体积：5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public String upload(MultipartFile file) throws IOException {
        // 1. 空文件校验
        if (file.isEmpty()) {
            throw new BusinessException(1001, "文件不能为空");
        }

        // 2. 体积校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(1001, "图片大小不能超过 5MB");
        }

        // 3. MIME 类型校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(1001, "仅支持 JPG / PNG / GIF / WebP / BMP 格式的图片");
        }

        // 4. 扩展名校验
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(1001, "不支持的文件类型：" + ext);
        }

        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(dir, name);
        file.transferTo(dest);
        return "/uploads/" + name;
    }
}
