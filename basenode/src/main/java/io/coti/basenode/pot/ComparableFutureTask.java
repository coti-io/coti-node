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
        return Integer.compare(priority, o.priority);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ComparableFutureTask)) {
            return false;
        }
        return this.priority == priority;
    }

    @Override
    public int hashCode() {
        return priority;
    }
}
