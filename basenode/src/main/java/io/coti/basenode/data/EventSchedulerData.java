package io.coti.basenode.data;

import lombok.Data;

import java.time.Instant;
import java.util.function.Consumer;

@Data
public class EventSchedulerData {
    Instant eventTime;
    boolean waitTillTriggered;
    boolean cancelling;
    Thread thread;
    Consumer<String> toExecuteIfFinished;
    Consumer<String> toExecuteIfCancelled;

    public EventSchedulerData(Instant eventTime, boolean waitTillTriggered, Thread thread, Consumer<String> toExecuteIfFinished, Consumer<String> toExecuteIfCancelled) {
        this.eventTime = eventTime;
        this.waitTillTriggered = waitTillTriggered;
        this.thread = thread;
        this.toExecuteIfFinished = toExecuteIfFinished;
        this.toExecuteIfCancelled = toExecuteIfCancelled;
        this.cancelling = false;
    }

    public EventSchedulerData(EventSchedulerData eventSchedulerData, Instant eventTime, boolean waitTillTriggered){
        this.eventTime = eventTime;
        this.waitTillTriggered = waitTillTriggered;
        this.thread = eventSchedulerData.getThread();
        this.toExecuteIfFinished = eventSchedulerData.getToExecuteIfFinished();
        this.toExecuteIfCancelled = eventSchedulerData.getToExecuteIfCancelled();
        this.cancelling = eventSchedulerData.isCancelling();
    }

}
