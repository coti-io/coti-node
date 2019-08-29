package io.coti.storagenode.services;

import io.coti.storagenode.database.DbConnectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class InitializationService {

    @Autowired
    private DbConnectorService dbConnectorService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private BuildProperties buildProperties;

    @PostConstruct
    private void init() {
        try {
            log.info("Application name: {}, version: {}", buildProperties.getName(), buildProperties.getVersion());
            dbConnectorService.init();
            objectService.init();
        } catch (Exception e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            System.exit(SpringApplication.exit(applicationContext));
        }
    }
}