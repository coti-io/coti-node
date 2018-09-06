package io.coti.fullnode.services;

import io.coti.basenode.services.BaseNodeMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitorService extends BaseNodeMonitorService {

    @Autowired
    private PotWorkerService potWorkerService;

    @Override
    public void lastState() {

    }

    @Scheduled(initialDelay = 1000, fixedDelay = 60000)
    public void lastPot() {
        potWorkerService.monitorStatistics.forEach((bucketNumber, statistic) -> {
            if (statistic.getNumberOfTransaction() > 0)
                log.info("Proof of Trust Range= {}-{}, NumberOfTransaction = {}, AverageTime = {} ms",
                        bucketNumber - 10, bucketNumber, statistic.getNumberOfTransaction(), statistic.getAverage());
        });
    }
}
