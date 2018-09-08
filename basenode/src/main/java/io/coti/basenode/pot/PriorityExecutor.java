package io.coti.basenode.pot;

import java.util.concurrent.*;

public class PriorityExecutor extends ThreadPoolExecutor {
    private int maximumQueueSize;
    private int initialCorePoolSize;

    public PriorityExecutor(int corePoolSize, int maximumPoolSize, int maximumQueueSize) {
        super(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>());
        initialCorePoolSize = corePoolSize;
        this.maximumQueueSize = maximumQueueSize;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return (RunnableFuture<T>) callable;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return (RunnableFuture<T>) runnable;
    }

    public synchronized void changeCorePoolSize() {
        if (getQueue().size() > maximumQueueSize && getCorePoolSize() < getMaximumPoolSize()) {
            setCorePoolSize(getPoolSize() + 1);
        } else if (getQueue().size() == 0) {
            setCorePoolSize(initialCorePoolSize);
        }
    }
}
