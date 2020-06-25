package io.coti.basenode.services;

import io.coti.basenode.data.VotingTimeoutData;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class VotingTimeoutService {

    ConcurrentHashMap<String, VotingTimeoutData> eventMap;

    private class VotingTimeoutServiceThreadLoop implements Runnable {

        private String eventID;

        public VotingTimeoutServiceThreadLoop(String eventID) {
            this.eventID = eventID;
        }

        public void run() {
            VotingTimeoutData votingTimeoutData = eventMap.get(eventID);
            try {
                Thread.sleep(votingTimeoutData.getTimeout() * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            votingTimeoutData = eventMap.get(eventID);
            eventMap.remove(eventID);
            if (votingTimeoutData.getToExecuteIfFinished() != null) {
                votingTimeoutData.getToExecuteIfFinished().accept(eventID);
            }
        }
    }

    public void init() {
        eventMap = new ConcurrentHashMap<>();
    }

    public void scheduleEvent(String eventID, long timeout, Consumer<String> toExecuteIfFinished) {
        Thread thread = new Thread(new VotingTimeoutServiceThreadLoop(eventID));
        eventMap.put(eventID, new VotingTimeoutData(timeout, thread, toExecuteIfFinished));
        thread.start();
    }

    public void cancelEvent(String eventID) {
        VotingTimeoutData votingTimeoutData = eventMap.get(eventID);
        if (votingTimeoutData != null && votingTimeoutData.getThread().isAlive()) {
            votingTimeoutData.getThread().interrupt();
            eventMap.remove(eventID);
        }
    }

    public void shutdown() {
        eventMap.values().forEach(e -> e.getThread().interrupt());
    }

}
