package io.coti.basenode;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class BaseNodeAppConfig {

    @Bean
    public TaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

}
