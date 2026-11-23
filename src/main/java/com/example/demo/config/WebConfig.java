package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

// 웹 설정: 업로드 이미지 경로 + 다국어 (한국어 / 日本語)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LangSaveInterceptor langSaveInterceptor;

    public WebConfig(LangSaveInterceptor langSaveInterceptor) {
        this.langSaveInterceptor = langSaveInterceptor;
    }

    // ① /upload/** 주소로 업로드 폴더의 이미지 보여 주기
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///C:/pzm-upload/");
    }

    // ② 고른 언어를 세션에 기억 (처음엔 한국어)
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);
        return resolver;
    }

    // ③ 인터셉터 등록 (위에서부터 순서대로 실행)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 주소에 ?lang=ja 가 붙으면 화면 언어 바꾸기
        LocaleChangeInterceptor localeChange = new LocaleChangeInterceptor();
        localeChange.setParamName("lang");
        registry.addInterceptor(localeChange);

        // 로그인 중이면 그 언어를 DB(members.lang)에도 저장
        registry.addInterceptor(langSaveInterceptor);
    }
}