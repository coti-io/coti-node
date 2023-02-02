package io.coti.storagenode.services;

import io.coti.basenode.exceptions.CotiRunTimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static io.coti.storagenode.services.NodeServiceManager.*;

@Slf4j
@Service
public class InitializationService {

    @Autowired
    public BuildProperties buildProperties;

    @PostConstruct
    private void init() {
        try {
            log.info("Application name: {}, version: {}", buildProperties.getName(), buildProperties.getVersion());
            databaseConnector.init();
            objectService.init();
        } catch (CotiRunTimeException e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            e.logMessage();
            System.exit(SpringApplication.exit(applicationContext));
        } catch (Exception e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            System.exit(SpringApplication.exit(applicationContext));
        }
    }
}