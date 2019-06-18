package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.IShutDownService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeShutDownService implements IShutDownService {
    @Autowired
    protected IConfirmationService confirmationService;
    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    protected IPropagationSubscriber propagationSubscriber;
    @Autowired
    protected IDatabaseConnector databaseConnector;

    public void shutdown() {
        shutDownServices();
    }

    public void shutDownServices() {
        propagationSubscriber.shutdown();
        propagationPublisher.shutdown();
        confirmationService.shutdown();
        databaseConnector.shutdown();
    }
}
