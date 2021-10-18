package io.coti.storagenode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class StorageNodeApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("Storage Node Main");
        SpringApplication.run(StorageNodeApplication.class, args);
        log.info("############################################################");
        log.info("#############    COTI STORAGE NODE IS UP       ################");
        log.info("############################################################");
    }
}
