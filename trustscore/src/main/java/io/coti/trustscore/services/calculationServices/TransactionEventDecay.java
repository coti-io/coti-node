package io.coti.trustscore.services.calculationServices;

import io.coti.trustscore.config.rules.TransactionEventScore;
import lombok.Data;

@Data
public class TransactionEventDecay{


    private TransactionEventScore transactionEventScore;
    private double  eventContributionValue;

    public TransactionEventDecay(TransactionEventScore transactionEventScore, double eventContributionValue){

        this.transactionEventScore = transactionEventScore;
        this.eventContributionValue = eventContributionValue;
    }

}
