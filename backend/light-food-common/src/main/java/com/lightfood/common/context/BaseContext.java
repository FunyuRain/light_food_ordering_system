package com.lightfood.common.context;

/**
 * Request-scoped holder for the authenticated principal.
 */
public final class BaseContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new IllegalStateException("No authenticated user in the current request");
        }
        return user.id();
    }

    public static String getRole() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.role();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
