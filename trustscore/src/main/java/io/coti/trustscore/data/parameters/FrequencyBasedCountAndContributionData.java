package io.coti.trustscore.data.parameters;

import lombok.Data;

import java.io.Serializable;

@Data
public class FrequencyBasedCountAndContributionData implements Serializable {

    private static final long serialVersionUID = -8462656629015970025L;
    private int countCurrent;
    private double contributionCurrent;
    private double contributionTail;

    public FrequencyBasedCountAndContributionData(int countCurrent, double contributionCurrent, double contributionTail) {
        this.countCurrent = countCurrent;
        this.contributionCurrent = contributionCurrent;
        this.contributionTail = contributionTail;
    }
}
