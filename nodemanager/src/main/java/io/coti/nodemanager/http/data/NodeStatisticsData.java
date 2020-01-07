package io.coti.nodemanager.http.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodeStatisticsData implements Serializable {
    private long nodeUpSecond;
    private long nodeRestarts;
    private long nodeDownTimes;

    public NodeStatisticsData() {
    }

    public NodeStatisticsData(long nodeUpSecond, int nodeRestarts, int nodeDownTimes) {
        this.nodeUpSecond = nodeUpSecond;
        this.nodeRestarts = nodeRestarts;
        this.nodeDownTimes = nodeDownTimes;
    }
}
