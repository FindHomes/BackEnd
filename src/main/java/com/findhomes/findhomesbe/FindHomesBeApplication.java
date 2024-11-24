package com.findhomes.findhomesbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.findhomes.findhomesbe")
public class FindHomesBeApplication {
    public static void main(String[] args) {
        SpringApplication.run(FindHomesBeApplication.class, args);
    }
}
