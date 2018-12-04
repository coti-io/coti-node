package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEventScore extends EventScore {

    private int term;

    private String contributionFormula;

    public void setContribution(String contributionFormula) {
        this.contributionFormula = contributionFormula;
    }

    public String getContributionFormula() {
        return contributionFormula;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

}
