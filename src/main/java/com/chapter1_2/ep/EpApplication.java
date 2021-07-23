package com.chapter1_2.ep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thymeleaf.TemplateEngine;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class EpApplication {

    public static void main(String[] args) {
        SpringApplication.run(EpApplication.class, args);
    }

}
