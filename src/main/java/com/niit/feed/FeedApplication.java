package com.niit.feed;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.niit.feed.mapper")
@EnableScheduling
public class FeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedApplication.class, args);
    }
}
