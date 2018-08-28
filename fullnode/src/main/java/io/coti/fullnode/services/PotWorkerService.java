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
import java.util.LinkedHashMap;
import java.util.concurrent.*;

@Slf4j
@Service
public class PotWorkerService extends PotService {

    private static HashMap<Integer, ExecutorService> queuesPot = new HashMap<>();
    public static HashMap<Integer,MonitorBuscketStatistics> monitorStatistics = new LinkedHashMap<>();

    @PostConstruct
    public void init() {

        for (int i = 10; i <= 100; i=i+10) {
            monitorStatistics.put(i,new MonitorBuscketStatistics());
            queuesPot.put(i, PriorityExecutor.newFixedThreadPool( (int)Math.floor(i/20), i/10));
        }
    }

    public void potAction(TransactionData transactionData) throws InterruptedException {

        int trustScore = transactionData.getRoundedSenderTrustScore();

        int bucketChoice = (int)(Math.ceil(trustScore/10) * 10);
        queuesPot.get(bucketChoice).submit(new ComparableFutureTask(new PotRunnableTask(transactionData, targetDifficulty)));
        Instant starts = Instant.now();
        synchronized (transactionData) {
            transactionData.wait();
        }
        Instant ends = Instant.now();
        monitorStatistics.get(bucketChoice).addTransactionStatistics(Duration.between(starts, ends));
    }




}

