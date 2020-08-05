package io.coti.trustscore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class TrustScoreApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("TrustScore Main");
        SpringApplication.run(TrustScoreApplication.class, args);
        log.info("############################################################");
        log.info("#############  COTI TRUST SCORE NODE IS UP  ################");
        log.info("############################################################");
    }
}