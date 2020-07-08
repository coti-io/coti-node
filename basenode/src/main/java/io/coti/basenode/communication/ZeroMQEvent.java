package io.coti.basenode.communication;

import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public enum ZeroMQEvent {
    CONNECTED(ZMQ.EVENT_CONNECTED, true),
    CONNECT_DELAYED(ZMQ.EVENT_CONNECT_DELAYED, false),
    CONNECT_RETRIED(ZMQ.EVENT_CONNECT_RETRIED, false),
    LISTENING(ZMQ.EVENT_LISTENING, true),
    BIND_FAILED(ZMQ.EVENT_BIND_FAILED, true),
    ACCEPTED(ZMQ.EVENT_ACCEPTED, true),
    ACCEPT_FAILED(ZMQ.EVENT_ACCEPT_FAILED, true),
    CLOSED(ZMQ.EVENT_CLOSED, false),
    CLOSE_FAILED(ZMQ.EVENT_CLOSE_FAILED, false),
    DISCONNECTED(ZMQ.EVENT_DISCONNECTED, true),
    MONITOR_STOPPED(ZMQ.EVENT_MONITOR_STOPPED, true),
    HANDSHAKE_PROTOCOL(ZMQ.EVENT_HANDSHAKE_PROTOCOL, true),
    ALL(ZMQ.EVENT_ALL, false);

    private final boolean displayLog;

    private static class Events {
        private static final Map<Integer, ZeroMQEvent> eventNumberToEventMap = new HashMap<>();
    }

    ZeroMQEvent(int eventNumber, boolean displayLog) {
        Events.eventNumberToEventMap.put(eventNumber, this);
        this.displayLog = displayLog;
    }

    public boolean isDisplayLog() {
        return displayLog;
    }

    public static ZeroMQEvent getEvent(int eventNumber) {
        return Events.eventNumberToEventMap.get(eventNumber);
    }
}
