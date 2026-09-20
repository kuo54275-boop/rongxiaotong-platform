package com.rongxiaotong.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件上传接口：头像、商品图等
 * 文件保存到本地 uploads 目录，返回可访问的 URL
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    /** 上传图片 */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BizException("请选择要上传的文件");
        }
        // 只允许常见图片格式
        String ext = FileUtil.extName(file.getOriginalFilename()).toLowerCase();
        if (!"jpg".equals(ext) && !"jpeg".equals(ext) && !"png".equals(ext)
                && !"gif".equals(ext) && !"webp".equals(ext) && !"svg".equals(ext)) {
            throw new BizException("只支持 jpg / jpeg / png / gif / webp / svg 格式的图片");
        }
        try {
            // 随机文件名，避免重名覆盖
            String filename = IdUtil.fastSimpleUUID() + "." + ext;
            // 必须转成绝对路径：transferTo 会把相对路径解析到 Tomcat 临时目录（坑）
            File dir = new File(uploadDir).getAbsoluteFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.transferTo(new File(dir, filename));
            return Result.ok("上传成功", "/api/uploads/" + filename);
        } catch (Exception e) {
            throw new BizException("上传失败：" + e.getMessage());
        }
    }
}
