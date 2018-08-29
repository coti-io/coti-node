package io.coti.fullnode.services;
import io.coti.basenode.services.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FullNodeMonitorService extends MonitorService {

    @Autowired
    private PotWorkerService potWorkerService;

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void lastState() {
        potWorkerService.monitorStatistics.forEach((bucketNumber, statistic) -> {
            if (statistic.getNumberOfTransaction() > 0)
                log.info("Bucket Range={}-{}, NumberOfTransaction = {}, AverageTime = {} ms",
                        bucketNumber-10,bucketNumber,statistic.getNumberOfTransaction(),statistic.getAverage());
        });
    }
}
