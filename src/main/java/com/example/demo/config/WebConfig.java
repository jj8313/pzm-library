package com.example.demo.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 업로드 폴더 경로 (yml의 upload.dir 값)
    @Value("${upload.dir}")
    private String uploadDir;

    private final LangSaveInterceptor langSaveInterceptor;

    public WebConfig(LangSaveInterceptor langSaveInterceptor) {
        this.langSaveInterceptor = langSaveInterceptor;
    }

    // /upload/** 주소로 업로드 폴더의 이미지 보여 주기
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadDir);
    }

    // 언어를 세션에 기억 (처음엔 한국어)
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);
        return resolver;
    }

    // ?lang=ko / ?lang=ja 로 언어 바꾸기 + 로그인 회원이면 DB에 저장
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChange = new LocaleChangeInterceptor();
        localeChange.setParamName("lang");
        registry.addInterceptor(localeChange);
        registry.addInterceptor(langSaveInterceptor);
    }
}