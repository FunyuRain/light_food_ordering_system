package com.lightfood.common;

import com.lightfood.common.constant.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {

    @Test
    void mapStatusText() {
        assertEquals("待付款", OrderStatus.text(OrderStatus.PENDING_PAYMENT));
        assertEquals("待接单", OrderStatus.text(OrderStatus.PENDING_ACCEPT));
        assertEquals("已接单", OrderStatus.text(OrderStatus.ACCEPTED));
        assertEquals("已完成", OrderStatus.text(OrderStatus.COMPLETED));
        assertEquals("已取消", OrderStatus.text(OrderStatus.CANCELLED));
    }

    @Test
    void validateStatusCode() {
        assertTrue(OrderStatus.isValid(0));
        assertTrue(OrderStatus.isValid(4));
        assertFalse(OrderStatus.isValid(5));
        assertFalse(OrderStatus.isValid(null));
    }
}
