package com.wip.couriertrackingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.wip")
@EntityScan(basePackages = "com.wip.entity")
@EnableJpaRepositories(basePackages = "com.wip.repository")
public class CouriertrackingsystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouriertrackingsystemApplication.class, args);
        System.out.println("Application Started Successfully");
    }
}