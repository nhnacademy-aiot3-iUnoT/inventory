package com.nhnacademy.inventory.global.config;

import com.nhnacademy.inventory.global.interceptor.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;

    public WebMvcConfig(UserInterceptor userInterceptor){
        this.userInterceptor = userInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/api/core/**");
    }
}
