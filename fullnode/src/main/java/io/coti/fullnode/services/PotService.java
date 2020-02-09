package io.coti.fullnode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.pot.ComparableFutureTask;
import io.coti.basenode.pot.PotRunnableTask;
import io.coti.basenode.pot.PriorityExecutor;
import io.coti.basenode.services.BaseNodePotService;
import io.coti.fullnode.data.MonitorBucketStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class PotService extends BaseNodePotService {

    private static HashMap<Integer, ExecutorService> queuesPot = new HashMap<>();
    protected static HashMap<Integer, MonitorBucketStatistics> monitorStatistics = new LinkedHashMap<>();

    public void init() {
        for (int i = 10; i <= 100; i = i + 10) {
            monitorStatistics.put(i, new MonitorBucketStatistics());
            queuesPot.put(i, new PriorityExecutor(i / 10, i / 5, 5 + (100 - i) / 2));
        }
        super.init();
    }

    public void potAction(TransactionData transactionData) {

        int trustScore = transactionData.getRoundedSenderTrustScore();

        int bucketChoice = (int) (Math.ceil((double) trustScore / 10) * 10);
        if (queuesPot.get(bucketChoice) == null) {
            throw new IllegalArgumentException("Illegal trust score");
        }
        ((PriorityExecutor) queuesPot.get(bucketChoice)).changeCorePoolSize();
        queuesPot.get(bucketChoice).submit(new ComparableFutureTask(new PotRunnableTask(transactionData, targetDifficulty)));
        Instant starts = Instant.now();
        synchronized (transactionData) {
            try {
                transactionData.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Instant ends = Instant.now();
        monitorStatistics.get(bucketChoice).addTransactionStatistics(Duration.between(starts, ends));
    }

    public HashMap<String, Integer> executorSizes(int bucketNumber) {
        PriorityExecutor executor = (PriorityExecutor) queuesPot.get(bucketNumber);
        HashMap<String, Integer> executorSizes = new HashMap<>();
        executorSizes.put("ActiveThreads", executor.getActiveCount());
        executorSizes.put("MaximumPoolSize", executor.getMaximumPoolSize());
        executorSizes.put("QueueSize", executor.getQueue().size());
        return executorSizes;
    }


}

