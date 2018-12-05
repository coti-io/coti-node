package io.coti.trustscore.config.rules;

public abstract class EventScore {

    private String name;

    private double weight;

    private String decayFormula;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getDecayFormula() {
        return decayFormula;
    }

    public void setDecay(String decay) {
        this.decayFormula = decay;
    }


}
