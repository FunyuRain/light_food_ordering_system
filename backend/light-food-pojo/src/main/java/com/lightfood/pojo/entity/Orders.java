package com.lightfood.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Orders {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long addressId;
    private Integer status;
    private BigDecimal totalAmount;
    private Integer payMethod;
    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private Integer cancelBy;
    private String cancelReason;
    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
