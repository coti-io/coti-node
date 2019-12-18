package io.coti.trustscore.data.events;

import lombok.Data;

import java.io.Serializable;

@Data
public class EventCountAndContributionData implements Serializable {

    private static final long serialVersionUID = 3457883359863824154L;
    private int count;
    private double contribution;

    public EventCountAndContributionData(int count, double contribution) {
        this.count = count;
        this.contribution = contribution;
    }
}
