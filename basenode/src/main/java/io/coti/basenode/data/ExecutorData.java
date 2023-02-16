package io.coti.basenode.data;

import io.coti.basenode.exceptions.TransactionSyncException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Data
public class ExecutorData {

    private final ExecutorService executorService;
    private final List<Future<?>> futures;
    private final InitializationTransactionHandlerType initializationTransactionHandlerType;
    private AtomicLong futuresToComplete;
    private AtomicLong failedFutures;

    public ExecutorData(InitializationTransactionHandlerType initializationTransactionHandlerType) {
        executorService = Executors.newFixedThreadPool(1);
        futures = Collections.synchronizedList(new ArrayList<>());
        this.initializationTransactionHandlerType = initializationTransactionHandlerType;
        futuresToComplete = new AtomicLong(0);
        failedFutures = new AtomicLong(0);
    }

    public void submit(Runnable runnable) {
        try {
            futuresToComplete.incrementAndGet();
            futures.add(executorService.submit(runnable));
        } catch (Exception exception) {
            log.error("Error while running " + initializationTransactionHandlerType + " executor.\n", exception);
            failedFutures.incrementAndGet();
        }
    }

    public void waitForTermination(AtomicLong transactionsToProcess) {
        AtomicInteger completedFutures = new AtomicInteger(0);
        Thread monitorCompletionThread = getMonitorCompletionThread(completedFutures,transactionsToProcess);
        monitorCompletionThread.start();
        while (transactionsToProcess.get() != futuresToComplete.get() && failedFutures.get() == 0) {
            LockSupport.parkNanos(500_000_000);
        }
        for (Future<?> future : futures) {
            try {
                future.get();
                completedFutures.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("Error while waiting for futures completion for " + initializationTransactionHandlerType + " executor.\n", e);
                failedFutures.incrementAndGet();
            }
        }
        executorService.shutdown();
        monitorCompletionThread.interrupt();
        try {
            monitorCompletionThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (failedFutures.get() > 0) {
            throw new TransactionSyncException("There are " + failedFutures.get() + " transactions that got exception while processing by " + initializationTransactionHandlerType + " handler.");
        }
    }

    private Thread getMonitorCompletionThread(AtomicInteger completedFutures, AtomicLong transactionsToProcess) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                logCompleted(completedFutures, transactionsToProcess);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Init " + initializationTransactionHandlerType);
    }

    private void logCompleted(AtomicInteger completedFutures, AtomicLong transactionsToProcess) {
        log.info("Initial handler type {}. Total : {}, Completed : {}", initializationTransactionHandlerType, transactionsToProcess, completedFutures);
    }
}
