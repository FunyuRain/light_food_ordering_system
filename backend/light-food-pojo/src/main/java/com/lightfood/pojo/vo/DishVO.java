package com.lightfood.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DishVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
