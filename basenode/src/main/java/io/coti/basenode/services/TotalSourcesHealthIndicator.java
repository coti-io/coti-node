package io.coti.basenode.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TotalSourcesHealthIndicator implements HealthIndicator {

    private static final String TOTAL_SOURCES_DETAIL = "TotalSources";
    @Autowired
    private ClusterService clusterService;

    @Override
    public Health health() {
        long checkedValue = check();
        Health.Builder builder;
        if (checkedValue > 15) {
            builder = new Health.Builder().down();
            return builder.withDetail("internalStatus", "Critical").withDetail(TOTAL_SOURCES_DETAIL, checkedValue).build();
        } else if (checkedValue > 8) {
            builder = new Health.Builder().up();
            return builder.withDetail("internalStatus", "WARNING").withDetail(TOTAL_SOURCES_DETAIL, checkedValue).build();
        }

        builder = new Health.Builder().up();
        return builder.withDetail("internalStatus", "NORMAL").withDetail(TOTAL_SOURCES_DETAIL, checkedValue).withDetail("stam", 7).build();
    }

    private long check() {
        return clusterService.getTotalSources();
    }
}
