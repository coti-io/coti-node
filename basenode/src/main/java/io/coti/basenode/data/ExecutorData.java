package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class ExecutorData {

    private ExecutorService executorService;
    private List<Future<?>> futures;
    private InitializationTransactionHandlerType initializationTransactionHandlerType;

    public ExecutorData(InitializationTransactionHandlerType initializationTransactionHandlerType) {
        executorService = Executors.newFixedThreadPool(1);
        futures = new ArrayList<>();
        this.initializationTransactionHandlerType = initializationTransactionHandlerType;
    }

    public void submit(Runnable runnable) {
        futures.add(executorService.submit(runnable));
    }

    public void waitForTermination() {
        AtomicInteger completedFutures = new AtomicInteger(0);
        Thread monitorCompletionThread = getMonitorCompletionThread(completedFutures);
        monitorCompletionThread.start();
        for (Future<?> future : futures) {
            try {
                future.get();
                completedFutures.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error(e.getMessage());
            }
        }
        monitorCompletionThread.interrupt();
        try {
            monitorCompletionThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Thread getMonitorCompletionThread(AtomicInteger completedFutures) {
        return new Thread(() -> {
            logCompleted(completedFutures);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                logCompleted(completedFutures);
            }
        }, "Init " + initializationTransactionHandlerType);
    }

    private void logCompleted(AtomicInteger completedFutures) {
        log.info("Initial handler type {}. Total : {}, Completed : {}", initializationTransactionHandlerType, futures.size(), completedFutures);
    }
}
