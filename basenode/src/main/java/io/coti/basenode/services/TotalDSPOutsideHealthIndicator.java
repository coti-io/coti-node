package io.coti.basenode.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TotalDSPOutsideHealthIndicator implements HealthIndicator {

    @Autowired
    private BaseNodeMonitorService baseNodeMonitorService;

    @Override
    public Health health() {
        int counter = check();
        Health.Builder builder;
        if (counter > 5) {
            builder = new Health.Builder().down();
            return builder.withDetail("internalStatus", "Critical").withDetail("DSPOutside", counter).build();
        }
        builder = new Health.Builder().up();
        return builder.withDetail("internalStatus", "NORMAL").withDetail("DSPOutside", counter).withDetail("stam", 7).build();
    }

    private int check() {
        return baseNodeMonitorService.getDspOutsideNormalCounter();
    }
}
