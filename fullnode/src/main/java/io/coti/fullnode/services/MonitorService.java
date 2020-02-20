package io.coti.fullnode.services;

import io.coti.basenode.services.BaseNodeMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class MonitorService extends BaseNodeMonitorService {

    @Autowired
    private PotService potService;

    @Override
    public void lastState() {
        // implemented by sub classes
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 60000)
    public void lastPot() {
        PotService.monitorStatistics.forEach((bucketNumber, statistic) -> {
            if (statistic.getNumberOfTransaction() > 0) {
                HashMap<String, Integer> executorSizes = potService.executorSizes(bucketNumber);
                log.info("Proof of Trust Range= {}-{}, NumberOfTransaction = {}, AverageTime = {} ms, ActiveThreads = {}, MaximumPoolSize = {}, QueueSize = {}",
                        bucketNumber - 10, bucketNumber, statistic.getNumberOfTransaction(), statistic.getAverage(), executorSizes.get("ActiveThreads"), executorSizes.get("MaximumPoolSize"), executorSizes.get("QueueSize"));
            }
        });
    }
}
