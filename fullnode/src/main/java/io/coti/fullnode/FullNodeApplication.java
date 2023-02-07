package io.coti.fullnode;

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
public class FullNodeApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("FullNode Main");
        SpringApplication.run(FullNodeApplication.class, args);
        log.info("############################################################");
        log.info("#############    COTI FULL NODE IS UP       ################");
        log.info("############################################################");
    }

    @PreDestroy
    public void destroy() {
        log.info("!!!!!!!!!!!!!  COTI FULL NODE IS DOWN !!!!!!!!!!!!!!!");
    }
}