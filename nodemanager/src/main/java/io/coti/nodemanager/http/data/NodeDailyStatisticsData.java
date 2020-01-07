package io.coti.nodemanager.http.data;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class NodeDailyStatisticsData implements Serializable {
    private LocalDate recordDate;
    private long nodeUpSecond;
    private long nodeRestarts;
    private long nodeDownTimes;

    public NodeDailyStatisticsData() {
    }

    public NodeDailyStatisticsData(LocalDate recordDate, long nodeUpSecond, int nodeRestarts, int nodeDownTimes) {
        this.recordDate = recordDate;
        this.nodeUpSecond = nodeUpSecond;
        this.nodeRestarts = nodeRestarts;
        this.nodeDownTimes = nodeDownTimes;
    }
}
