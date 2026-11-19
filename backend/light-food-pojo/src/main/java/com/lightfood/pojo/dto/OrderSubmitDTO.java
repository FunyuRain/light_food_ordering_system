package com.lightfood.pojo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderSubmitDTO {

    @NotNull(message = "请选择收货地址")
    private Long addressId;

    @Size(max = 200, message = "订单备注不能超过200个字符")
    private String remark;
}
