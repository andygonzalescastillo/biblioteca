package com.biblioteca.backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final AppProperties appProperties;

    @Bean
    public Cloudinary cloudinary() {
        AppProperties.Cloudinary config = appProperties.cloudinary();
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", config.cloudName(),
                "api_key", config.apiKey(),
                "api_secret", config.apiSecret(),
                "secure", true
        ));
    }
}

