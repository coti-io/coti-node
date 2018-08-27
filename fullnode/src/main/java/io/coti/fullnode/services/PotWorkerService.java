package io.coti.fullnode.services;

import io.coti.common.pot.ComparableFutureTask;
import io.coti.common.pot.PriorityExecutor;
import io.coti.common.pot.PotRunnableTask;
import io.coti.common.data.TransactionData;
import io.coti.common.services.PotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.*;

@Slf4j
@Service
public class PotWorkerService extends PotService {

    private static HashMap<Integer, ExecutorService> queuesPot = new HashMap<>();
    public static HashMap<Integer,MonitorBuscketStatistics> monitorStatistics = new HashMap<>();


    @PostConstruct
    public void init() {
        queuesPot.put(5, PriorityExecutor.newFixedThreadPool(8, 10));
        queuesPot.put(4, PriorityExecutor.newFixedThreadPool(3, 5));
        queuesPot.put(3, PriorityExecutor.newFixedThreadPool(3, 4));
        queuesPot.put(2, PriorityExecutor.newFixedThreadPool(3, 4));
        queuesPot.put(1, PriorityExecutor.newFixedThreadPool(1, 1));

        for (int i = 1; i < 6; i++) {
            monitorStatistics.put(i,new MonitorBuscketStatistics());
        }
    }

    public void potAction(TransactionData transactionData) throws InterruptedException {
        int bucketChoice;
        int trustScore = transactionData.getRoundedSenderTrustScore();
        if (trustScore >= 90)
            bucketChoice = 5;
        else if (trustScore >= 60)
            bucketChoice = 4;
        else if (trustScore >= 40)
            bucketChoice = 3;
        else if (trustScore >= 20)
            bucketChoice = 2;
        else
            bucketChoice = 1;

        queuesPot.get(bucketChoice).submit(new ComparableFutureTask(new PotRunnableTask(transactionData, targetDifficulty)));
        Instant starts = Instant.now();
        synchronized (transactionData) {
            transactionData.wait();
        }
        Instant ends = Instant.now();
        monitorStatistics.get(bucketChoice).addTransactionStatistics(Duration.between(starts, ends));
    }




}

