package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HighFrequencyEventScore extends EventScore {

    private String linearFunction;

    private double standardChargeBackRate;

    private String contribution;

    private String term;

    public String getContribution() {
        return contribution;
    }

    public void setContribution(String contribution) {
        this.contribution = contribution;
    }

    public String getLinearFunction() {
        return linearFunction;
    }

    public void setLinearFunction(String linearFunction) {
        this.linearFunction = linearFunction;
    }

    public double getStandardChargeBackRate() {
        return standardChargeBackRate;
    }

    public void setStandardChargeBackRate(double standardChargeBackRate) {
        this.standardChargeBackRate = standardChargeBackRate;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

}
