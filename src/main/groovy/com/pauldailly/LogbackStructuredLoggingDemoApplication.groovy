package com.pauldailly

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class LogbackStructuredLoggingDemoApplication {

    static void main(String[] args) {
        SpringApplication.run LogbackStructuredLoggingDemoApplication, args
        new ParentJobProcess().executeParentJob(3)
    }
}
