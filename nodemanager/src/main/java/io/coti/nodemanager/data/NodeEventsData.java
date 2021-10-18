package io.coti.nodemanager.data;

import lombok.Data;

import java.util.List;

@Data
public class NodeEventsData {

    private List<NodeNetworkDataRecord> events;
    private NodeNetworkDataRecord previousEvent;
    private NodeNetworkDataRecord nextEvent;

    public NodeEventsData(List<NodeNetworkDataRecord> events, NodeNetworkDataRecord previousEvent, NodeNetworkDataRecord nextEvent) {
        this.events = events;
        this.previousEvent = previousEvent;
        this.nextEvent = nextEvent;
    }
}
