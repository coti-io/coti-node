package io.coti.nodemanager.data;

import lombok.Data;

@Data
public class NodeActivityData {

    private long activityUpTimeInSeconds;
    private long numberOfDays;

    public NodeActivityData(long activityUpTimeInSeconds, long numberOfDays) {
        this.activityUpTimeInSeconds = activityUpTimeInSeconds;
        this.numberOfDays = numberOfDays;
    }
}
