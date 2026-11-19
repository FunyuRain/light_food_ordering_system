package com.lightfood.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private Long addressId;
    private Integer status;
    private String statusText;
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
    private List<OrderItemVO> items;
}
