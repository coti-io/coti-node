package io.coti.trustscore.data.scorebuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreevents.BalanceBasedScoreData;
import lombok.Data;

@Data
public class BucketDepositBalanceBasedScoreData extends BucketData<BalanceBasedScoreData> implements IEntity {

    private static final long serialVersionUID = 1879770617138638423L;
    private double currentBalance = 0.0;
    private double currentBalanceContribution = 0.0;
    private double currentDayClose = 0.0;
    private double tail = 0.0;

    public BucketDepositBalanceBasedScoreData() {
        super();
    }
}