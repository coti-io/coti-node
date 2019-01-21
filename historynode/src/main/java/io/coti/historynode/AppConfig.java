package io.coti.historynode;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.historynode", "io.coti.basenode"}
)
@EnableScheduling
@PropertySource("classpath:application.properties")
//@Configuration
//@ComponentScan(
//        basePackages = {"io.coti.historynode", "io.coti.basenode"}
//)
//@EnableScheduling
//@PropertySource("classpath:application.properties")
public class AppConfig {
}
