package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQContext;
import io.coti.basenode.services.interfaces.IShutDownService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Slf4j
@Service
public class BaseNodeShutDownService implements IShutDownService {

    public void shutdown() {
        ZeroMQContext.terminate();
        shutDownServices();
    }

    public void shutDownServices() {
        zeroMQReceiver.shutdown();
        propagationSubscriber.shutdown();
        propagationPublisher.shutdown();
        zeroMQSender.shutdown();
        confirmationService.shutdown();
        databaseConnector.shutdown();
    }
}
