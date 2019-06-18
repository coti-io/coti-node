package io.coti.basenode.pot;

import java.util.concurrent.FutureTask;

public class ComparableFutureTask extends FutureTask<Void> implements Comparable<ComparableFutureTask> {

    private volatile int priority;

    public ComparableFutureTask(PotRunnableTask runnable) {
        super(runnable, null);
        this.priority = runnable.getPriority();
    }

    @Override
    public int compareTo(ComparableFutureTask o) {
        return Integer.valueOf(priority).compareTo(o.priority);
    }
}
