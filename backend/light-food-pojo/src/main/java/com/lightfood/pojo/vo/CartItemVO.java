package com.lightfood.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long id;
    private Long dishId;
    private Long categoryId;
    private String dishName;
    private String dishImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
    private Integer status;
}
