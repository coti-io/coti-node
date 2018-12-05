package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompensableEventScore extends EventScore {

    private int term;
    private double weight1;
    private double weight2;
    private String contributionFormula;
    private String fine;
    private String fineDailyChange;

    public String getFineFormula() {
        return fine;
    }

    public void setFine(String fine) {
        this.fine = fine;
    }

    public String getFineDailyChange() {
        return fineDailyChange;
    }

    public void setFineDailyChange(String fineDailyChange) {
        this.fineDailyChange = fineDailyChange;
    }

    public double getWeight1() {
        return weight1;
    }

    public void setWeight1(double weight1) {
        this.weight1 = weight1;
    }

    public double getWeight2() {
        return weight2;
    }

    public void setWeight2(double weight2) {
        this.weight2 = weight2;
    }


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