package io.coti.trustscore.data.parameters;

import io.coti.trustscore.config.rules.UserScoreRules;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserParameters implements Serializable {

    public static final double atanh08 = 1.098612289;
    private static final long serialVersionUID = -3910619954455987859L;
    private double weight;
    private long semiDecay;

    public UserParameters() {
    }

    public UserParameters(UserScoreRules userScoreRules) {
        this.weight = userScoreRules.getWeight();
        this.semiDecay = userScoreRules.getSemiDecay();
    }

}


