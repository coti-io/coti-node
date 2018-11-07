package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public abstract class EventScore {

    private String name;

    private double weight;

    private String decayFormula;

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "weight")
    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @XmlElement(name = "decay")
    public String getDecayFormula() {
        return decayFormula;
    }

    public void setDecayFormula(String decay) {
        this.decayFormula = decay;
    }


}
