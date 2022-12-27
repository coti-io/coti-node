package io.coti.zerospend;

import io.coti.basenode.utilities.MonitorConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;


@Slf4j
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(MonitorConfigurationProperties.class)
public class ZeroSpendServerApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("ZeroSpend Main");
        SpringApplication.run(ZeroSpendServerApplication.class, args);
        log.info("##################################################################");
        log.info("################    ZERO SPEND SERVER IS UP       ##################");
        log.info("##################################################################");
    }
}