package io.coti.trustscore.data.events;

import lombok.Data;

import java.io.Serializable;

@Data
public class BalanceCountAndContribution implements Serializable {

    private static final long serialVersionUID = 8623087199557981479L;
    private double count;
    private double contribution;

    public BalanceCountAndContribution(double count, double contribution) {
        this.count = count;
        this.contribution = contribution;
    }
}
