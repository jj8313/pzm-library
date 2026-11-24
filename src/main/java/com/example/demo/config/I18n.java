package com.example.demo.config;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component

public class I18n {
    private final MessageSource messageSource;

    public I18n(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // 키 + (있으면) 빈칸 값 {0}, {1} ...
    // 키를 못 찾으면 → 받은 글자를 그대로 돌려줌 (에러 안 남)
    public String get(String key, Object... values) {
        return messageSource.getMessage(key, values, key, LocaleContextHolder.getLocale());
    }
}
