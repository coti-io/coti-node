package io.coti.trustscore.data.parameters;

import io.coti.trustscore.config.rules.UserScoreRules;
import lombok.Data;

@Data
public class EventUserParameters extends UserParameters {

    private static final long serialVersionUID = 4288954283703901184L;
    private long term;

    public EventUserParameters(UserScoreRules userScoreRules) {
        super(userScoreRules);
        this.term = userScoreRules.getTerm();
    }
}


