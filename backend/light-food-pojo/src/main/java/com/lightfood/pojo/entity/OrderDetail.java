package com.lightfood.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetail {
    private Long id;
    private Long orderId;
    private Long dishId;
    private String dishName;
    private String dishImage;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal amount;
    private LocalDateTime createTime;
}
