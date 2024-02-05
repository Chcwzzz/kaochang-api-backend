package com.chcwzzz.myInterface;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.chcwzzz.myInterface.mapper")
public class KaoChangInterfaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(KaoChangInterfaceApplication.class, args);
    }
}