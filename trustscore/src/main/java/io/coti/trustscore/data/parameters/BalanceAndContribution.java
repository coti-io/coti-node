package io.coti.trustscore.data.parameters;

import lombok.Data;

import java.io.Serializable;

@Data
public class BalanceAndContribution implements Serializable {

    private static final long serialVersionUID = -5657702525950031815L;
    private double count;
    private double contribution;

    public BalanceAndContribution(double count, double contribution) {
        this.count = count;
        this.contribution = contribution;
    }
}
