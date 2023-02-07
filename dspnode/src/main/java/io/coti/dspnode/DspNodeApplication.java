package io.coti.dspnode;

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
public class DspNodeApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("DSP Main");
        SpringApplication.run(DspNodeApplication.class, args);
        log.info("############################################################");
        log.info("################    DSP NODE IS UP       ##################");
        log.info("############################################################");
    }

    @PreDestroy
    public void destroy() {
        log.info("!!!!!!!!!!!!!!!!    DSP NODE IS DOWN       !!!!!!!!!!!!!!!!!!");
    }

}