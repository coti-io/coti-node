package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.LockData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.pot.ComparableFutureTask;
import io.coti.basenode.pot.PotRunnableTask;
import io.coti.basenode.pot.PriorityExecutor;
import io.coti.basenode.services.BaseNodePotService;
import io.coti.fullnode.data.MonitorBucketStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@Primary
public class PotService extends BaseNodePotService {

    private static final HashMap<Integer, ExecutorService> queuesPot = new HashMap<>();
    protected static final HashMap<Integer, MonitorBucketStatistics> monitorStatistics = new LinkedHashMap<>();
    private final LockData transactionLockData = new LockData();

    @Override
    public void init() {
        for (int i = 10; i <= 100; i = i + 10) {
            monitorStatistics.put(i, new MonitorBucketStatistics());
            queuesPot.put(i, new PriorityExecutor(i / 10, i / 5, 5 + (100 - i) / 2));
        }
        super.init();
    }

    @Override
    public void potAction(TransactionData transactionData) {
        Hash transactionHash = transactionData.getHash();
        try {
            synchronized (transactionLockData.addLockToLockMap(transactionHash)) {
                final AtomicInteger lock = transactionLockData.getByHash(transactionHash);
                int trustScore = transactionData.getRoundedSenderTrustScore();

                int bucketChoice = (int) (Math.ceil((double) trustScore / 10) * 10);
                if (queuesPot.get(bucketChoice) == null) {
                    throw new IllegalArgumentException("Illegal trust score");
                }
                ((PriorityExecutor) queuesPot.get(bucketChoice)).changeCorePoolSize();
                queuesPot.get(bucketChoice).submit(new ComparableFutureTask(new PotRunnableTask(transactionData, targetDifficulty, lock)));
                Instant starts = Instant.now();

                while (transactionData.getNonces() == null) {
                    lock.wait(1000);
                }
                Instant ends = Instant.now();
                monitorStatistics.get(bucketChoice).addTransactionStatistics(Duration.between(starts, ends));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            transactionLockData.removeLockFromLocksMap(transactionHash);
        }
    }

    @Override
    public Map<String, Integer> executorSizes(Integer bucketNumber) {
        PriorityExecutor executor = (PriorityExecutor) queuesPot.get(bucketNumber);
        Map<String, Integer> executorSizes = new HashMap<>();
        executorSizes.put("ActiveThreads", executor.getActiveCount());
        executorSizes.put("MaximumPoolSize", executor.getMaximumPoolSize());
        executorSizes.put("QueueSize", executor.getQueue().size());
        return executorSizes;
    }


}

