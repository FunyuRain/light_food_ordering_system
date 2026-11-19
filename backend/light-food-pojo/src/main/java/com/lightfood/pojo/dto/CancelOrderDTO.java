package com.lightfood.pojo.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelOrderDTO {

    @Size(max = 200, message = "取消原因不能超过200个字符")
    private String reason;
}
