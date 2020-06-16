package io.coti.nodemanager.http.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodeStatisticsData implements Serializable {

    private long nodeUpTimeInSeconds;
    private long nodeRestarts;
    private long nodeDownTimes;

    private NodeStatisticsData() {
    }

    public NodeStatisticsData(long nodeUpTimeInSeconds, int nodeRestarts, int nodeDownTimes) {
        this.nodeUpTimeInSeconds = nodeUpTimeInSeconds;
        this.nodeRestarts = nodeRestarts;
        this.nodeDownTimes = nodeDownTimes;
    }
}
