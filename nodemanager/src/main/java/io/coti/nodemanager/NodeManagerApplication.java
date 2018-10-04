package io.coti.nodemanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class NodeManagerApplication {

    public static void main(String[] args) {

        SpringApplication.run(NodeManagerApplication.class, args);
        log.info("############################################################");
        log.info("#############    COTI NODE MANAGER IS UP    ################");
        log.info("############################################################");
    }
}