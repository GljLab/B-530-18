package com.example.permission.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String appHome = System.getenv("APP_HOME");
        String uploadPath;
        if (appHome != null && !appHome.isEmpty()) {
            uploadPath = "file:" + appHome + "/uploads/";
        } else {
            uploadPath = "file:" + System.getProperty("user.dir") + "/uploads/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // LocalDate 转换器 - 支持 yyyy-MM-dd 格式
        registry.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(source.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd, got: " + source);
                }
            }
        });

        // LocalDateTime 转换器 - 支持 yyyy-MM-dd HH:mm:ss 格式
        registry.addConverter(new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                try {
                    // 尝试解析完整的日期时间格式
                    return LocalDateTime.parse(source.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e1) {
                    try {
                        // 尝试 ISO 格式 yyyy-MM-ddTHH:mm:ss
                        return LocalDateTime.parse(source.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("Invalid datetime format. Expected: yyyy-MM-dd HH:mm:ss or yyyy-MM-ddTHH:mm:ss, got: " + source);
                    }
                }
            }
        });
    }
}
