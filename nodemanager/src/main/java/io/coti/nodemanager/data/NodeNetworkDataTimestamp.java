package io.coti.nodemanager.data;

import io.coti.basenode.data.NetworkNodeData;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class NodeNetworkDataTimestamp implements Serializable {
    private String date;
    private NetworkNodeData networkNodeData;


    public NodeNetworkDataTimestamp(Instant instant, NetworkNodeData networkNodeData) {
        this.date = instant.toString();
        this.networkNodeData = networkNodeData;
    }
}
