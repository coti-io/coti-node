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

@Slf4j
@Data
public class ExecutorData {

    private final ExecutorService executorService;
    private final List<Future<?>> futures;
    private final InitializationTransactionHandlerType initializationTransactionHandlerType;
    private AtomicLong numberSubmittedTasks;
    private AtomicLong numberFailedTasks;
    private AtomicInteger numberCompletedTasks;

    public ExecutorData(InitializationTransactionHandlerType initializationTransactionHandlerType) {
        executorService = Executors.newFixedThreadPool(1);
        futures = Collections.synchronizedList(new ArrayList<>());
        this.initializationTransactionHandlerType = initializationTransactionHandlerType;
        numberSubmittedTasks = new AtomicLong(0);
        numberFailedTasks = new AtomicLong(0);
        numberCompletedTasks = new AtomicInteger(0);
    }

    public void skipTask() {
        numberSubmittedTasks.incrementAndGet();
        numberCompletedTasks.incrementAndGet();
    }

    public void submit(Runnable runnable) {
        try {
            futures.add(executorService.submit(runnable));
            numberSubmittedTasks.incrementAndGet();
        } catch (Exception exception) {
            log.error("Error while running " + initializationTransactionHandlerType + " executor.\n", exception);
            numberFailedTasks.incrementAndGet();
        }
    }

    private boolean waitForSubmitCompletion(AtomicLong completeNumberOfTransactions, AtomicLong lastNumberSubmittedTasks, AtomicInteger numberTimesNoProgress) {
        if (lastNumberSubmittedTasks.get() < this.numberSubmittedTasks.get()) {
            lastNumberSubmittedTasks.set(this.numberSubmittedTasks.get());
            numberTimesNoProgress.set(0);
            return true;
        } else {
            numberTimesNoProgress.incrementAndGet();
        }
        return completeNumberOfTransactions.get() != this.numberSubmittedTasks.get() && numberFailedTasks.get() == 0 && numberTimesNoProgress.get() < 10;
    }

    private void validateFuturesSubmitCompleted(AtomicLong completeNumberOfTransactions) {
        AtomicInteger numberTimesNoProgress = new AtomicInteger(0);
        AtomicLong lastNumberSubmittedTasks = new AtomicLong(this.numberSubmittedTasks.get());
        while (waitForSubmitCompletion(completeNumberOfTransactions, lastNumberSubmittedTasks, numberTimesNoProgress)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TransactionSyncException(String.format("Error while sleep waiting for submission to complete, error: %s", e));
            }
        }
        if (numberFailedTasks.get() > 0) {
            throw new TransactionSyncException(String.format("There are %s transactions that got exception while processing by %s handler.", numberFailedTasks.get(), initializationTransactionHandlerType));
        }
        if (completeNumberOfTransactions.get() != this.numberSubmittedTasks.get()) {
            throw new TransactionSyncException(String.format("There is a critical difference between number of transactions (%s) to number of tasks launched to handle (%s)", completeNumberOfTransactions.get(), this.numberSubmittedTasks.get()));
        }
    }

    private void validateFuturesCompleted(AtomicInteger numberCompletedTasks) {
        for (Future<?> future : futures) {
            try {
                future.get();
                numberCompletedTasks.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("Error while waiting for futures completion for " + initializationTransactionHandlerType + " executor.\n", e);
                numberFailedTasks.incrementAndGet();
            }
        }
        if (numberFailedTasks.get() > 0) {
            throw new TransactionSyncException(String.format("There are %s transactions that got exception while processing by %s handler.", numberFailedTasks.get(), initializationTransactionHandlerType));
        }
        if (numberCompletedTasks.get() != this.numberSubmittedTasks.get()) {
            throw new TransactionSyncException(String.format("There is a critical difference between number of launched tasks (%s) to number of successfully processed transactions (%s)", numberSubmittedTasks.get(), numberCompletedTasks.get()));
        }
    }

    public void waitForTermination(AtomicLong completeNumberOfTransactions) {
        Thread monitorCompletionThread = getMonitorCompletionThread(numberCompletedTasks, completeNumberOfTransactions);
        monitorCompletionThread.start();

        try {
            validateFuturesSubmitCompleted(completeNumberOfTransactions);
            validateFuturesCompleted(numberCompletedTasks);
        } catch (TransactionSyncException e) {
            log.error(e.toString());
            throw e;
        } finally {
            executorService.shutdown();
            monitorCompletionThread.interrupt();
            try {
                monitorCompletionThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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
