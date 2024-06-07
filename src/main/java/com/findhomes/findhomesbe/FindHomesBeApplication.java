package com.findhomes.findhomesbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.findhomes.findhomesbe.repository")
public class FindHomesBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindHomesBeApplication.class, args);
    }

}
