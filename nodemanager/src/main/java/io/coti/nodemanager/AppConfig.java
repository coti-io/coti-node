package io.coti.nodemanager;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.nodemanager"}
)

public class AppConfig {
}
