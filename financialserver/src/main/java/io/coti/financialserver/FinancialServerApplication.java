package io.coti.financialserver;

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
public class FinancialServerApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("Financial Server Main");
        SpringApplication.run(FinancialServerApplication.class, args);
        log.info("############################################################");
        log.info("#############    FINANCIAL SERVER IS UP       ##############");
        log.info("############################################################");
    }

    @PreDestroy
    public void destroy() {
        log.info("!!!!!!!!!!!!!  FINANCIAL SERVER IS DOWN !!!!!!!!!!!!!!!");
    }
}