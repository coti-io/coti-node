package io.coti.common.Infrastructure;
import java.util.concurrent.*;

public class PriorityExecutor extends ThreadPoolExecutor {

    public PriorityExecutor(int corePoolSize, int maximumPoolSize,
                            long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new PriorityExecutor(nThreads, nThreads, 0L,
                TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return (RunnableFuture<T>) callable;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return (RunnableFuture<T>) runnable;
    }
}
