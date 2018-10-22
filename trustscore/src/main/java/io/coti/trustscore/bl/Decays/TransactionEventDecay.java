package io.coti.trustscore.bl.Decays;

import io.coti.trustscore.rulesData.TransactionEventScore;
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
