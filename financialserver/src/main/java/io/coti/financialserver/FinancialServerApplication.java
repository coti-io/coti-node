package io.coti.financialserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class FinancialServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(FinancialServerApplication.class, args);
        log.info("############################################################");
        log.info("#############    FINANCIAL SERVER IS UP       ##############");
        log.info("############################################################");
    }
}