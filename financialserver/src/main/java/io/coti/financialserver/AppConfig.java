package io.coti.financialserver;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.financialserver", "io.coti.basenode"}
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
}
