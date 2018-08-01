package io.coti.fullnode;

import io.coti.common.communication.ZeroMQPropagationPublisher;
import io.coti.common.communication.ZeroMQReceiver;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.fullnode", "io.coti.common"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE, value = {ZeroMQPropagationPublisher.class, ZeroMQReceiver.class} )
        }
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
}
