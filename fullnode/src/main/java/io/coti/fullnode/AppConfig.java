package io.coti.fullnode;

import io.coti.common.services.ZeroMQTransactionPropagationPublisher;
import io.coti.common.services.ZeroMQTransactionReceiver;
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
                        type = FilterType.ASSIGNABLE_TYPE, value = {ZeroMQTransactionPropagationPublisher.class, ZeroMQTransactionReceiver.class} )
        }
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
}
