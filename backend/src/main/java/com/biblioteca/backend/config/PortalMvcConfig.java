package com.biblioteca.backend.config;

import com.biblioteca.backend.mvc.portal.PortalSessionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class PortalMvcConfig implements WebMvcConfigurer {

    private final PortalSessionInterceptor portalSessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(portalSessionInterceptor)
                .addPathPatterns("/portal/**")
                .excludePathPatterns(
                        "/portal/login",
                        "/portal/registro",
                        "/portal/css/**",
                        "/portal/js/**",
                        "/portal/images/**"
                );
    }
}
