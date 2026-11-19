package com.lightfood.common;

import com.lightfood.common.context.LoginUser;
import com.lightfood.common.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void generateAndParseToken() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 1);
        LoginUser principal = new LoginUser(7L, "USER", "测试用户");

        String token = jwtUtil.generate(principal);
        LoginUser parsed = jwtUtil.parse(token);

        assertEquals(principal, parsed);
    }

    @Test
    void rejectTamperedToken() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 1);
        String token = jwtUtil.generate(new LoginUser(1L, "ADMIN", "admin"));

        assertThrows(JwtException.class, () -> jwtUtil.parse(token + "x"));
    }

    @Test
    void rejectShortSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("short", 1));
    }
}
