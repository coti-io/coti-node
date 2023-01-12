package io.coti.basenode.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Configuration
@Slf4j
public class AppReadyConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void appReady() {
        propagationPublisher.initMonitor();
        propagationSubscriber.initMonitor();
        zeroMQReceiver.initMonitor();
        zeroMQSender.initMonitor();
        monitorService.initNodeMonitor();
        scraperService.initMonitor();
    }
}
