package io.coti.trustscore.data.contributiondata;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BalanceAndContribution implements Serializable {

    private static final long serialVersionUID = -5657702525950031815L;
    private BigDecimal count;
    private double contribution;

    public BalanceAndContribution(BigDecimal count, double contribution) {
        this.count = count;
        this.contribution = contribution;
    }
}
