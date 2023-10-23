package com.shesha4572.cdnslave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CdnSlaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdnSlaveApplication.class, args);
    }

}
