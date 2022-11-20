package com.wust.endpoint.chain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 区块链存证demo启动类
 * @author yibi
 * @date 2021-06-24 9:20
 */
@MapperScan("com.wust.endpoint.chain.persist.mapper")
@SpringBootApplication
public class EndpointApplication {

    public static void main(String[] args) {
        SpringApplication.run(EndpointApplication.class, args);
    }

}
