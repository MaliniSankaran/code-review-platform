package com.codereview.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class CodeReviewPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewPlatformApplication.class, args);
    }

}
