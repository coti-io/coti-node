package io.coti.nodemanager.services;

import io.coti.basenode.services.BaseNodeShutDownService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import static io.coti.nodemanager.services.NodeServiceManager.*;

@Service
@Primary
public class ShutDownService extends BaseNodeShutDownService {

    @Override
    public void shutDownServices() {
        healthCheckService.shutdown();
        propagationPublisher.shutdown();
        databaseConnector.shutdown();
    }
}
