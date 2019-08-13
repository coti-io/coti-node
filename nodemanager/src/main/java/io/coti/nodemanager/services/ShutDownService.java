package io.coti.nodemanager.services;

import io.coti.basenode.services.BaseNodeShutDownService;
import io.coti.nodemanager.services.interfaces.IHealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShutDownService extends BaseNodeShutDownService {

    @Autowired
    private IHealthCheckService healthCheckService;

    @Override
    public void shutDownServices() {
        healthCheckService.shutdown();
        propagationPublisher.shutdown();
        databaseConnector.shutdown();
    }
}
