package io.coti.trustscore.data.Events;

import lombok.Data;

import java.io.Serializable;

@Data
public class BalanceCountAndContribution implements Serializable {
    private double count;
    private double contribution;

    public BalanceCountAndContribution(double count, double contribution) {
        this.count = count;
        this.contribution = contribution;
    }
}
