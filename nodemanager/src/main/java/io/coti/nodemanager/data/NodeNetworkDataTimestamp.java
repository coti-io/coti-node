package io.coti.nodemanager.data;

import io.coti.basenode.data.NetworkNodeData;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class NodeNetworkDataTimestamp implements Serializable {
    private String date;
    private NetworkNodeData networkNodeData;


    public NodeNetworkDataTimestamp(LocalDateTime date, NetworkNodeData networkNodeData) {
        this.date = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.networkNodeData = networkNodeData;
    }
}
