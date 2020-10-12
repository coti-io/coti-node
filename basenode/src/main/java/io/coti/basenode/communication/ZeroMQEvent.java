package io.coti.basenode.communication;

import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public enum ZeroMQEvent {
    CONNECTED(ZMQ.EVENT_CONNECTED, true, false),
    CONNECT_DELAYED(ZMQ.EVENT_CONNECT_DELAYED, false, false),
    CONNECT_RETRIED(ZMQ.EVENT_CONNECT_RETRIED, false, false),
    LISTENING(ZMQ.EVENT_LISTENING, true, true),
    BIND_FAILED(ZMQ.EVENT_BIND_FAILED, true, true),
    ACCEPTED(ZMQ.EVENT_ACCEPTED, true, false),
    ACCEPT_FAILED(ZMQ.EVENT_ACCEPT_FAILED, true, false),
    CLOSED(ZMQ.EVENT_CLOSED, false, false),
    CLOSE_FAILED(ZMQ.EVENT_CLOSE_FAILED, false, false),
    DISCONNECTED(ZMQ.EVENT_DISCONNECTED, true, true),
    MONITOR_STOPPED(ZMQ.EVENT_MONITOR_STOPPED, true, true),
    HANDSHAKE_PROTOCOL(ZMQ.EVENT_HANDSHAKE_PROTOCOL, true, false),
    ALL(ZMQ.EVENT_ALL, false, false);

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
