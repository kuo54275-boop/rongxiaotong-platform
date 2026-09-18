package com.rongxiaotong;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类：整个后端的入口
 * 运行方式：在 backend 目录执行 mvn spring-boot:run
 */
@SpringBootApplication
@MapperScan("com.rongxiaotong.mapper")  // 扫描 Mapper 接口所在的包
public class RongXiaoTongApplication {

    public static void main(String[] args) {
        SpringApplication.run(RongXiaoTongApplication.class, args);
        System.out.println("====== 融小通平台后端启动成功：http://localhost:8080/api ======");
    }
}
