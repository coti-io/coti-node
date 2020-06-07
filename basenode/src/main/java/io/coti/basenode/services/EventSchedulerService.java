package io.coti.basenode.services;

import io.coti.basenode.data.EventSchedulerData;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class EventSchedulerService {

    ConcurrentHashMap<String, EventSchedulerData> eventMap;

    public class EventSchedulerServiceThreadLoop implements Runnable {

        private String eventID;
        private long granularity;

        public EventSchedulerServiceThreadLoop(String eventID, long granularity){
            this.eventID = eventID;
            this.granularity = granularity;
        }

        public void run(){
            EventSchedulerData eventSchedulerData = eventMap.get(eventID);
            while (eventSchedulerData.isWaitTillTriggered() || Instant.now().isBefore(eventSchedulerData.getEventTime())) {
                try {
                    Thread.sleep(granularity);
                } catch (Exception ignored) {
                    // ignored exception
                }
                if (Thread.currentThread().isInterrupted()){
                    if (eventSchedulerData.isCancelling() && eventSchedulerData.getToExecuteIfCancelled() != null) {
                        eventSchedulerData.getToExecuteIfCancelled().accept(eventID);
                    }
                    return;
                }
                eventSchedulerData = eventMap.get(eventID);
            }
            eventMap.remove(eventID);
            if (eventSchedulerData.getToExecuteIfFinished() != null) {
                eventSchedulerData.getToExecuteIfFinished().accept(eventID);
            }
        }
    }

    public void init(){
        eventMap = new ConcurrentHashMap<>();
    }

    public Thread scheduleEvent(String eventID, Instant eventTime, long granularity, boolean waitTillTriggered,
                                Consumer<String> toExecuteIfFinished, Consumer<String> toExecuteIfCancelled){
        Thread thread = new Thread(new EventSchedulerServiceThreadLoop(eventID, granularity));
        eventMap.put(eventID, new EventSchedulerData(eventTime, waitTillTriggered, thread, toExecuteIfFinished, toExecuteIfCancelled));
        thread.start();
        return thread;
    }

    public void modifyEvent(String eventID, Instant eventTime, boolean waitTillTriggered){
        EventSchedulerData eventSchedulerData = new EventSchedulerData(eventMap.get(eventID), eventTime, waitTillTriggered);
        eventMap.replace(eventID, eventSchedulerData);
    }

    public void cancelEvent(String eventID){
        EventSchedulerData eventSchedulerData = eventMap.get(eventID);
        if (eventSchedulerData != null) {
            eventSchedulerData.setCancelling(true);
            eventSchedulerData.getThread().interrupt();
            eventMap.remove(eventID);
        }
    }

    public void shutdown() {
        eventMap.values().forEach(e -> e.getThread().interrupt());

    }

}
