package io.coti.nodemanager;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan(
        basePackages = {"io.coti.nodemanager", "io.coti.basenode"}
)
public class AppConfig {

}
