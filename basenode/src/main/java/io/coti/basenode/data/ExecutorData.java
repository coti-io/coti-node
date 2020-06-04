package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Data
public class ExecutorData {

    private ExecutorService executorService;
    private List<Future<?>> futures;

    public ExecutorData() {
        executorService = Executors.newFixedThreadPool(1);
        futures = new ArrayList<>();
    }

    public void submit(Runnable runnable) {
        futures.add(executorService.submit(runnable));
    }

    public void waitForTermination() {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error(e.getMessage());
            }
        }
    }
}
