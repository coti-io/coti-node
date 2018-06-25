package io.coti.cotinode;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("io.coti.cotinode")
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {


}
