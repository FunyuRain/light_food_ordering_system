package com.lightfood.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddDTO {

    @NotNull(message = "菜品不能为空")
    private Long dishId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    @Max(value = 99, message = "单个菜品最多购买99份")
    private Integer quantity;
}
