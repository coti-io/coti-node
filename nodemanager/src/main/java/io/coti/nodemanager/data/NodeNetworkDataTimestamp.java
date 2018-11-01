package io.coti.nodemanager.data;

import io.coti.basenode.data.NetworkNodeData;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class NodeNetworkDataTimestamp implements Serializable {
    private LocalDateTime date;
    private NetworkNodeData networkNodeData;

    @Override
    public String toString() {
        return "NodeNetworkDataTimestamp{" +
                "date=" + date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                ", networkNodeData=" + networkNodeData +
                '}';
    }


    public NodeNetworkDataTimestamp(LocalDateTime date, NetworkNodeData networkNodeData) {
        this.date = date;
        this.networkNodeData = networkNodeData;
    }
}
