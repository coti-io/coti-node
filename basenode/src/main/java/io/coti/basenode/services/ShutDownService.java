package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.IBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
public class ShutDownService {
    @Autowired
    IBalanceService balanceService;
    @Autowired
    IPropagationPublisher propagationPublisher;
    @Autowired
    IPropagationSubscriber propagationSubscriber;
    @Autowired
    IDatabaseConnector databaseConnector;

    @PreDestroy
    public void shutDownServices() {
        propagationPublisher.shutdown();
        propagationSubscriber.shutdown();
        balanceService.shutdown();
        databaseConnector.shutdown();
    }
}
