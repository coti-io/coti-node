package io.coti.fullnode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.pot.ComparableFutureTask;
import io.coti.basenode.pot.PotRunnableTask;
import io.coti.basenode.services.PotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class PotWorkerService extends PotService {

    private static HashMap<Integer, ExecutorService> queuesPot = new HashMap<>();
    public static HashMap<Integer, MonitorBucketStatistics> monitorStatistics = new LinkedHashMap<>();

    @PostConstruct
    public void init() {

        for (int i = 10; i <= 100; i = i + 10) {
            monitorStatistics.put(i, new MonitorBucketStatistics());
            queuesPot.put(i, io.coti.common.pot.PriorityExecutor.newFixedThreadPool((int) Math.floor(i / 20), i / 10));
        }
    }

    public void potAction(TransactionData transactionData) throws InterruptedException {

        int trustScore = transactionData.getRoundedSenderTrustScore();

        int bucketChoice = (int) (Math.ceil(trustScore / 10) * 10);
        queuesPot.get(bucketChoice).submit(new ComparableFutureTask(new PotRunnableTask(transactionData, targetDifficulty)));
        Instant starts = Instant.now();
        synchronized (transactionData) {
            transactionData.wait();
        }
        Instant ends = Instant.now();
        monitorStatistics.get(bucketChoice).addTransactionStatistics(Duration.between(starts, ends));
    }


}

