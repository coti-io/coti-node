package io.coti.trustscore.data.parameters;

import io.coti.trustscore.config.rules.UserScoreRules;
import lombok.Data;

@Data
public class FrequencyBasedUserParameters extends UserParameters {

    private static final long serialVersionUID = 5590723688617940504L;
    private long term;
    private double level08;
    private int period;
    private int limit;

    public FrequencyBasedUserParameters(UserScoreRules userScoreRules) {
        super(userScoreRules);
        this.level08 = userScoreRules.getLevel08();
        this.period = userScoreRules.getPeriod();
        this.term = userScoreRules.getTerm();
        this.limit = userScoreRules.getLimit();
    }
}


