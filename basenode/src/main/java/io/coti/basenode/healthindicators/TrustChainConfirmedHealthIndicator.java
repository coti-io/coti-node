package io.coti.basenode.healthindicators;

import io.coti.basenode.services.interfaces.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import static io.coti.basenode.http.BaseNodeHealthMetricConstants.TRUST_CHAIN_CONFIRMED_LABEL;

@Component
public class TrustChainConfirmedHealthIndicator implements HealthIndicator {

    @Autowired
    protected IMonitorService monitorService;

    @Override
    public Health health() {
        return monitorService.getHealthBuilder(TRUST_CHAIN_CONFIRMED_LABEL);
    }

}
