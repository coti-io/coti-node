package io.coti.trustscore.data.Events;

import lombok.Data;

import java.io.Serializable;

@Data
public class EventCountAndContributionData implements Serializable {
    private int count;
    private double contribution;

    public EventCountAndContributionData(int count, double contribution) {
        this.count = count;
        this.contribution = contribution;
    }
}
