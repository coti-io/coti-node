package io.coti.fullnode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class FullNodeApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("FullNode Main");
        SpringApplication.run(FullNodeApplication.class, args);
        log.info("############################################################");
        log.info("#############    COTI FULL NODE IS UP       ################");
        log.info("############################################################");
    }
}