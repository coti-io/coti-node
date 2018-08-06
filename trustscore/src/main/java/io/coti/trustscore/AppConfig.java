package io.coti.trustscore;

import io.coti.common.services.ZeroMQPropagationPublisher;
import io.coti.common.services.ZeroMQReceiver;
import io.coti.common.services.ZeroMQSender;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.trustscore", "io.coti.common"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE, value = {ZeroMQPropagationPublisher.class, ZeroMQReceiver.class, ZeroMQSender.class})
        })

@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
}
