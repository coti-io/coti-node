package io.coti.dspnode.services;

import io.coti.basenode.services.BaseNodeShutDownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShutDownService extends BaseNodeShutDownService {

    @Autowired
    private TransactionService transactionService;

    @Override
    public void shutDownServices() {
        receiver.shutdown();
        propagationSubscriber.shutdown();
        propagationPublisher.shutdown();
        transactionService.shutdown();
        confirmationService.shutdown();
        databaseConnector.shutdown();
    }
}
