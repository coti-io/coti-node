package io.coti.trustscore.data.parameters;

import lombok.Data;

import java.io.Serializable;

@Data
public class EventCountAndContributionData implements Serializable {

    private static final long serialVersionUID = -2506907592330188901L;
    private int countCurrent;
    private double contribution;

    public EventCountAndContributionData(int countCurrent, double contribution) {
        this.countCurrent = countCurrent;
        this.contribution = contribution;
    }
}
