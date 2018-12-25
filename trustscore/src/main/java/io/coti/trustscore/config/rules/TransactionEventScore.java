package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEventScore extends EventScore {



    private String nonlinearFunction;

    public String getNonlinearFunction() {
        return nonlinearFunction;
    }

    public void setNonlinearFunction(String nonlinearFunction) {
        this.nonlinearFunction = nonlinearFunction;
    }
}
