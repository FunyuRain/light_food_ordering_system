package com.lightfood.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminRegisterDTO {

    @NotBlank(message = "管理员用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需为3-50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需为6-64个字符")
    private String password;

    @NotBlank(message = "请再次输入密码")
    private String confirmPassword;

    @Size(max = 50, message = "管理员姓名不能超过50个字符")
    private String realName;
}
