package io.coti.zerospend;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.zerospend", "io.coti.common"}
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
}