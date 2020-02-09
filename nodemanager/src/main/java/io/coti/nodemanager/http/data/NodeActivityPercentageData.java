package io.coti.nodemanager.http.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodeActivityPercentageData implements Serializable {
    boolean status;
    double percentage;

    public NodeActivityPercentageData() {
    }

    public NodeActivityPercentageData(boolean status, double percentage) {
        this.status = status;
        this.percentage = percentage;
    }
}
