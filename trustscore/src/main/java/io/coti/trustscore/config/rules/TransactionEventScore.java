package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transactionScore")
public class TransactionEventScore extends EventScore {

    private String idea;

    private String nonlinearFunction;

    @XmlElement(name = "idea")
    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    @XmlElement(name = "nonlinearFunction")
    public String getNonlinearFunction() {
        return nonlinearFunction;
    }

    public void setNonlinearFunction(String nonlinearFunction) {
        this.nonlinearFunction = nonlinearFunction;
    }
}
