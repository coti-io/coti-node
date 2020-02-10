package io.coti.nodemanager.http.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodeActivityPercentageData implements Serializable {

    protected boolean status;
    protected double percentage;

    protected NodeActivityPercentageData() {
    }

    public NodeActivityPercentageData(double percentage) {
        this.status = true;
        this.percentage = percentage;
    }
}
