package io.coti.dspnode.services;

import io.coti.basenode.services.BaseNodeShutDownService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import static io.coti.dspnode.services.NodeServiceManager.*;

@Service
@Primary
public class ShutDownService extends BaseNodeShutDownService {

    @Override
    public void shutDownServices() {
        zeroMQReceiver.shutdown();
        propagationSubscriber.shutdown();
        propagationPublisher.shutdown();
        transactionService.shutdown();
        zeroMQSender.shutdown();
        confirmationService.shutdown();
        databaseConnector.shutdown();
    }
}
