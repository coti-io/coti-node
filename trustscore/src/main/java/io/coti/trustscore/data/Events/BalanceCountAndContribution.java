package io.coti.trustscore.data.Events;

import lombok.Data;

@Data
public class BalanceCountAndContribution {
    private double count;
    private double contribution;

    public BalanceCountAndContribution(double count, double contribution) {
        this.count = count;
        this.contribution = contribution;
    }
}
