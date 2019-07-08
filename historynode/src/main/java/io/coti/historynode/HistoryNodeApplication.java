package io.coti.historynode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class HistoryNodeApplication {

    public static void main(String[] args) {

        SpringApplication.run(HistoryNodeApplication.class, args);
        log.info("############################################################");
        log.info("#############    COTI HISTORY NODE IS UP       ################");
        log.info("############################################################");
    }
}
