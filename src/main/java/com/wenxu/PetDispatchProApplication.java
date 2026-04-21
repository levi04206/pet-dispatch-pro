package com.wenxu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.wenxu.mapper")
public class PetDispatchProApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetDispatchProApplication.class, args);
    }
}
