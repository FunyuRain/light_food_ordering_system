package com.lightfood.pojo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishDTO {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 100, message = "菜品名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "菜品描述不能超过500个字符")
    private String description;

    @Size(max = 500, message = "图片路径过长")
    private String imageUrl;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.00", message = "价格不能小于0")
    @Digits(integer = 8, fraction = 2, message = "价格最多8位整数和2位小数")
    private BigDecimal price;

    private Integer sort;

    @NotNull(message = "上架状态不能为空")
    @Min(value = 0, message = "上架状态不正确")
    @Max(value = 1, message = "上架状态不正确")
    private Integer status;
}
