package io.coti.trustscore.data.contributiondata;


import lombok.Data;

import java.io.Serializable;

@Data
public class CounteragentBalanceContributionData implements Serializable {

    private static final long serialVersionUID = -7928926126857673731L;
    private double currentBalance;
    private boolean repayment;
    private double fine;
    private double oldFine;
    private double tail;

    public CounteragentBalanceContributionData(double currentDebt, boolean repayment, double fine, double oldFine, double tail) {
        this.currentBalance = currentDebt;
        this.repayment = repayment;
        this.fine = fine;
        this.oldFine = oldFine;
        this.tail = tail;
    }
}
