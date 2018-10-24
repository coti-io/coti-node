package io.coti.nodemanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class NodeManagerApplication {

    public static void main(String[] args) {

        SpringApplication.run(NodeManagerApplication.class, args);
        log.info("############################################################");
        log.info("#############     NODE Manager IS UP        ################");
        log.info("############################################################");
    }
}