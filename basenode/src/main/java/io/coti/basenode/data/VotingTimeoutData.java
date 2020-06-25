package io.coti.basenode.data;

import lombok.Data;

import java.util.function.Consumer;

@Data
public class VotingTimeoutData {
    long timeout;
    boolean cancelling;
    Thread thread;
    Consumer<String> toExecuteIfFinished;

    public VotingTimeoutData(long timeout, Thread thread, Consumer<String> toExecuteIfFinished) {
        this.timeout = timeout;
        this.thread = thread;
        this.toExecuteIfFinished = toExecuteIfFinished;
        this.cancelling = false;
    }

}
