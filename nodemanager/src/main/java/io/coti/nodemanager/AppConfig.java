package io.coti.nodemanager;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.communication.ZeroMQPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
public class AppConfig {
    @Bean
    public IPropagationPublisher propagationPublisher() {
        return new ZeroMQPropagationPublisher();
    }

    @Bean
    public ISerializer serializer(){
        return new JacksonSerializer();
    }
}
