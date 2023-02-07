package io.coti.trustscore;

import io.coti.basenode.utilities.MonitorConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PreDestroy;

@Slf4j
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(MonitorConfigurationProperties.class)
public class TrustScoreApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("TrustScore Main");
        SpringApplication.run(TrustScoreApplication.class, args);
        log.info("############################################################");
        log.info("#############  COTI TRUST SCORE NODE IS UP  ################");
        log.info("############################################################");
    }

    @PreDestroy
    public void destroy() {
        log.info("!!!!!!!!!!!!!  COTI TRUST SCORE NODE IS DOWN !!!!!!!!!!!!!!!");
    }
}