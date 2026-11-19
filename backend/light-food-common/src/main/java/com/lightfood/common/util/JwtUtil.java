package com.lightfood.common.util;

import com.lightfood.common.context.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * Stateless JWT creation and parsing.
 */
public class JwtUtil {

    private static final String ISSUER = "light-food";

    private final SecretKey key;
    private final Duration ttl;

    public JwtUtil(String secret, long expireHours) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }
        if (expireHours <= 0) {
            throw new IllegalArgumentException("JWT expire hours must be positive");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofHours(expireHours);
    }

    public String generate(LoginUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl.toMillis());
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(user.id()))
                .claim("role", user.role())
                .claim("name", user.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public LoginUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long id = Long.valueOf(claims.getSubject());
        String role = claims.get("role", String.class);
        String name = claims.get("name", String.class);
        return new LoginUser(id, role, name);
    }
}
