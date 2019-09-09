package io.coti.trustscore.data.parameters;

import io.coti.trustscore.config.rules.UserScoreRules;
import lombok.Data;

@Data
public class TransactionUserParameters extends UserParameters {

    private static final long serialVersionUID = 7226037057894340316L;
    private double numberWeight;
    private double numberLevel08;
    private long numberSemiDecay;
    private double turnoverWeight;
    private double turnoverLevel08;
    private long turnoverSemiDecay;
    private double balanceWeight;
    private double balanceLevel08;
    private long balanceSemiDecay;

    public TransactionUserParameters() {}

    public TransactionUserParameters(UserScoreRules userScoreRules) {
        super(userScoreRules);
        this.numberWeight = userScoreRules.getNumberWeight();
        this.numberLevel08 = userScoreRules.getNumberLevel08();
        this.numberSemiDecay = userScoreRules.getNumberSemiDecay();
        this.turnoverWeight = userScoreRules.getTurnoverWeight();
        this.turnoverLevel08 = userScoreRules.getTurnoverLevel08();
        this.turnoverSemiDecay = userScoreRules.getTurnoverSemiDecay();
        this.balanceWeight = userScoreRules.getBalanceWeight();
        this.balanceLevel08 = userScoreRules.getBalanceLevel08();
        this.balanceSemiDecay = userScoreRules.getBalanceSemiDecay();
    }
}


