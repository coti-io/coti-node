package io.coti.fullnode;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.fullnode", "io.coti.common"}
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
}
