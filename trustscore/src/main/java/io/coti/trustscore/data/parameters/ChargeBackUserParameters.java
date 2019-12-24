package io.coti.trustscore.data.parameters;

import io.coti.trustscore.config.rules.UserScoreRules;
import lombok.Data;

@Data
public class ChargeBackUserParameters extends UserParameters {

    private static final long serialVersionUID = 144264652406302112L;
    private double standardChargeBackRate;
    private double numberWeight1;
    private double numberWeight2;
    private long numberSemiDecay;
    private double amountWeight1;
    private double amountWeight2;
    private long amountSemiDecay;

    public ChargeBackUserParameters(UserScoreRules userScoreRules) {
        super(userScoreRules);
        this.standardChargeBackRate = userScoreRules.getStandardChargeBackRate();
        this.numberWeight1 = userScoreRules.getNumberWeight1();
        this.numberWeight2 = userScoreRules.getNumberWeight2();
        this.numberSemiDecay = userScoreRules.getNumberSemiDecay();
        this.amountWeight1 = userScoreRules.getAmountWeight1();
        this.amountWeight2 = userScoreRules.getAmountWeight2();
        this.amountSemiDecay = userScoreRules.getAmountSemiDecay();
    }
}


