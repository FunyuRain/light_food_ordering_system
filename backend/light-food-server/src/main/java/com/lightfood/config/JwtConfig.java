package com.lightfood.config;

import com.lightfood.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private final JwtProperties properties;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(properties.getSecret(), properties.getExpireHours());
    }
}
