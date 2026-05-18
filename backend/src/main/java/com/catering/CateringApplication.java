package com.catering;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.catering.mapper")
@EnableAsync
public class CateringApplication {
    public static void main(String[] args) {
        SpringApplication.run(CateringApplication.class, args);
    }
}
