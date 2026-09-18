package com.rongxiaotong.config;

import com.rongxiaotong.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web 配置：
 * 1. 注册登录拦截器，并放行不需要登录的接口
 * 2. 把本地 uploads 目录映射成网络路径 /uploads/**（让图片能被浏览器访问）
 * 3. 允许跨域（前端开发时端口不同）
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Value("${app.upload-dir}")
    private String uploadDir;

    /** 启动时确保上传目录存在 */
    @PostConstruct
    public void initUploadDir() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")                              // 所有接口都要检查登录
                .excludePathPatterns(
                        "/auth/login",       // 登录
                        "/auth/register",    // 注册
                        "/public/**",        // 公开接口（首页展示等）
                        "/uploads/**",       // 上传的图片
                        "/images/**"         // 内置的种子图片
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 浏览器访问 /api/uploads/xxx.jpg 时，实际读取本地 uploads 目录下的 xxx.jpg
        String absolutePath = new File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
