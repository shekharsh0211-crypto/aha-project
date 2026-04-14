package com.ahatravel.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.ahatravel.customer.repository")
public class AhaCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AhaCustomerApplication.class, args);
    }
}
