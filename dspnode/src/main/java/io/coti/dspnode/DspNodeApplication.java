package io.coti.dspnode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class DspNodeApplication {

    public static void main(String[] args) {

        SpringApplication.run(DspNodeApplication.class, args);
        log.info("############################################################");
        log.info("################    DSP NODE IS UP       ##################");
        log.info("############################################################");


    }
}