package com.lightfood.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginDTO {

    @NotBlank(message = "手机号或账号不能为空")
    @Size(max = 50, message = "手机号或账号过长")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 64, message = "密码过长")
    private String password;
}
