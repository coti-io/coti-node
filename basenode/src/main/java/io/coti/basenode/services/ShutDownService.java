package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.IConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
public class ShutDownService {
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IDatabaseConnector databaseConnector;

    @PreDestroy
    public void shutDownServices() {
        propagationPublisher.shutdown();
        propagationSubscriber.shutdown();
        confirmationService.shutdown();
        databaseConnector.shutdown();
    }
}
