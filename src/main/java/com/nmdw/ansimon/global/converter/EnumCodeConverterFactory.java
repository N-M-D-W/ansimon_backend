package com.nmdw.ansimon.global.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Component
public class EnumCodeConverterFactory implements ConverterFactory<String, EnumCode>, WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(this);
    }

    @Override
    public <T extends EnumCode> Converter<String, T> getConverter(Class<T> targetType) {
        T[] enumConstants = targetType.getEnumConstants();
        if (enumConstants == null) {
            throw new IllegalArgumentException("Target type must be an enum: " + targetType.getName());
        }

        return source -> Arrays.stream(enumConstants)
                .filter(enumConstant -> enumConstant.code().equals(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum code: " + source));
    }
}
