package com.lightfood.pojo.vo;

import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String label;
    private Integer isDefault;
    private String fullAddress;
}
