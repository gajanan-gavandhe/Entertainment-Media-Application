package com.streamverse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import java.nio.file.*;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);
            registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
        } catch (Exception e) {
            // ignore
        }
    }
}
