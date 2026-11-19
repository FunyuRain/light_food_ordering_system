package com.lightfood.common.context;

/**
 * Authenticated principal resolved from a JWT.
 */
public record LoginUser(Long id, String role, String name) {
}
