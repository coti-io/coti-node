package io.coti.fullnode;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("io.coti.fullnode")
@ComponentScan("io.coti.common")
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
}
