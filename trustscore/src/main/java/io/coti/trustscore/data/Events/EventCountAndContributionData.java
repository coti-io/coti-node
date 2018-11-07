package io.coti.trustscore.data.Events;

import lombok.Data;

@Data
public class EventCountAndContributionData {
    private int count;
    private double contribution;

    public EventCountAndContributionData(int count, double contribution) {
        this.count = count;
        this.contribution = contribution;
    }
}
