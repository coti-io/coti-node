package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "highFrequencyEventsScore")
public class HighFrequencyEventScore extends EventScore {

    @XmlElement(name = "linearFunction")
    private String linearFunction;

    @XmlElement(name = "standardChargeBackRate")
    private double standardChargeBackRate;

    @XmlElement(name = "contribution")
    private String contribution;

    @XmlElement(name = "term")
    private String term;

    public String getContribution() {
        return contribution;
    }

    public String getLinearFunction() {
        return linearFunction;
    }

    public double getStandardChargeBackRate() {
        return standardChargeBackRate;
    }

}
