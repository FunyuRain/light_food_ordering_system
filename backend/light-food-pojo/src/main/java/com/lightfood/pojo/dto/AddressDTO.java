package com.lightfood.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressDTO {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人不能超过50个字符")
    private String receiverName;

    @NotBlank(message = "收货电话不能为空")
    @Pattern(regexp = "^[0-9+\\- ]{6,20}$", message = "收货电话格式不正确")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份不能超过50个字符")
    private String province;

    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市不能超过50个字符")
    private String city;

    @NotBlank(message = "区县不能为空")
    @Size(max = 50, message = "区县不能超过50个字符")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址不能超过255个字符")
    private String detailAddress;

    @Size(max = 20, message = "地址标签不能超过20个字符")
    private String label;

    private Boolean isDefault;
}
