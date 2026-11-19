package com.lightfood.common.constant;

public final class OrderStatus {

    public static final int PENDING_PAYMENT = 0;
    public static final int PENDING_ACCEPT = 1;
    public static final int ACCEPTED = 2;
    public static final int COMPLETED = 3;
    public static final int CANCELLED = 4;

    private OrderStatus() {
    }

    public static boolean isValid(Integer status) {
        return status != null && status >= PENDING_PAYMENT && status <= CANCELLED;
    }

    public static String text(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case PENDING_PAYMENT -> "待付款";
            case PENDING_ACCEPT -> "待接单";
            case ACCEPTED -> "已接单";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            default -> "未知";
        };
    }
}
