package io.coti.storagenode;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.storagenode", "io.coti.basenode"}
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
}
