package com.wenxu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wenxu.mapper") // 🚨 新增这一行！告诉系统 Mapper 接口在哪个包下
public class PetDispatchProApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetDispatchProApplication.class, args);
    }

}
