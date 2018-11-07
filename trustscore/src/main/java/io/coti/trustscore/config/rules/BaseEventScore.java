package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "standardEventScore")
public class BaseEventScore extends EventScore {
    @XmlElement(name = "contribution")
    private String contributionFormula;

    @XmlElement(name = "term")
    private int term;

    public String getContributionFormula() {
        return contributionFormula;
    }

    public int getTerm() {
        return term;
    }

}
