package com.lightfood.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lightfood.jwt")
public class JwtProperties {
    private String secret;
    private long expireHours = 12;
}
