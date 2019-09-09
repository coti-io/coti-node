package io.coti.trustscore.data.parameters;

import io.coti.trustscore.config.rules.UserScoreRules;
import lombok.Data;

@Data
public class BalanceBasedUserParameters extends UserParameters {

    private static final long serialVersionUID = -6820857920238731901L;
    private double level08;
    private double weight1;
    private double weight2;

    public BalanceBasedUserParameters(UserScoreRules userScoreRules) {
        super(userScoreRules);
        this.level08 = userScoreRules.getLevel08();
        this.weight1 = userScoreRules.getWeight1();
        this.weight2 = userScoreRules.getWeight2();
    }
}

