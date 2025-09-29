// 사이트 설정

package com.example.clothing_backend.global;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // /images/** 요청이 들어오면 로컬 C:/upload/ 경로에서 파일 서빙
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/upload/");
    }
}