package com.lightfood.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Admin {
    private Long id;
    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
