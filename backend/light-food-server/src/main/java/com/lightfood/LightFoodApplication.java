package com.lightfood;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lightfood.mapper")
public class LightFoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(LightFoodApplication.class, args);
    }
}
