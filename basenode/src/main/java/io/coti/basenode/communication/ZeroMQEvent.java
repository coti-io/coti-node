package io.coti.basenode.communication;

import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public enum ZeroMQEvent {
    CONNECTED(ZMQ.EVENT_CONNECTED, true, true),
    CONNECT_DELAYED(ZMQ.EVENT_CONNECT_DELAYED, true, true),
    CONNECT_RETRIED(ZMQ.EVENT_CONNECT_RETRIED, true, true),
    LISTENING(ZMQ.EVENT_LISTENING, true, true),
    BIND_FAILED(ZMQ.EVENT_BIND_FAILED, true, true),
    ACCEPTED(ZMQ.EVENT_ACCEPTED, true, true),
    ACCEPT_FAILED(ZMQ.EVENT_ACCEPT_FAILED, true, true),
    CLOSED(ZMQ.EVENT_CLOSED, true, true),
    CLOSE_FAILED(ZMQ.EVENT_CLOSE_FAILED, true, true),
    DISCONNECTED(ZMQ.EVENT_DISCONNECTED, true, true),
    MONITOR_STOPPED(ZMQ.EVENT_MONITOR_STOPPED, true, true),
    HANDSHAKE_PROTOCOL(ZMQ.EVENT_HANDSHAKE_PROTOCOL, true, true),
    ALL(ZMQ.EVENT_ALL, true, true);

    private final boolean displayLog;
    private final boolean displayBeforeInit;

    private static class Events {
        private static final Map<Integer, ZeroMQEvent> eventNumberToEventMap = new HashMap<>();
    }

    ZeroMQEvent(int eventNumber, boolean displayLog, boolean displayBeforeInit) {
        Events.eventNumberToEventMap.put(eventNumber, this);
        this.displayLog = displayLog;
        this.displayBeforeInit = displayBeforeInit;
    }

    public boolean isDisplayLog() {
        return displayLog;
    }

    public boolean isDisplayBeforeInit() {
        return displayBeforeInit;
    }

    public static ZeroMQEvent getEvent(int eventNumber) {
        return Events.eventNumberToEventMap.get(eventNumber);
    }
}
