package io.coti.basenode.communication;

import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public enum ZeroMQEvent {
    CONNECTED(ZMQ.EVENT_CONNECTED),
    CONNECT_DELAYED(ZMQ.EVENT_CONNECT_DELAYED),
    CONNECT_RETRIED(ZMQ.EVENT_CONNECT_RETRIED),
    LISTENING(ZMQ.EVENT_LISTENING),
    BIND_FAILED(ZMQ.EVENT_BIND_FAILED),
    ACCEPTED(ZMQ.EVENT_ACCEPTED),
    ACCEPT_FAILED(ZMQ.EVENT_ACCEPT_FAILED),
    CLOSED(ZMQ.EVENT_CLOSED),
    CLOSE_FAILED(ZMQ.EVENT_CLOSE_FAILED),
    DISCONNECTED(ZMQ.EVENT_DISCONNECTED),
    MONITOR_STOPPED(ZMQ.EVENT_MONITOR_STOPPED),
    HANDSHAKE_PROTOCOL(ZMQ.EVENT_HANDSHAKE_PROTOCOL),
    ALL(ZMQ.EVENT_ALL);

    private static class Events {
        private static final Map<Integer, ZeroMQEvent> eventNumberToEventMap = new HashMap<>();
    }

    ZeroMQEvent(int eventNumber) {
        Events.eventNumberToEventMap.put(eventNumber, this);
    }

    public static ZeroMQEvent getEvent(int eventNumber) {
        return Events.eventNumberToEventMap.get(eventNumber);
    }
}
