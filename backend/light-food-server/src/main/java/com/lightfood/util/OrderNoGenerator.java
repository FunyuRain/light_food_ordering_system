package com.lightfood.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoGenerator() {
    }

    public static String generate() {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return LocalDateTime.now().format(FORMATTER) + random;
    }
}
