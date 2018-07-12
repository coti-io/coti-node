package io.coti.zero_spend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@Slf4j
@SpringBootApplication
@EnableAsync
public class ZeroSpendNodeApplication {

    public static void main(String[] args) {

        SpringApplication.run(ZeroSpendNodeApplication.class, args);
        log.info("##################################################################");
        log.info("################    ZERO SPEND NODE IS UP       ##################");
        log.info("##################################################################");
    }
}