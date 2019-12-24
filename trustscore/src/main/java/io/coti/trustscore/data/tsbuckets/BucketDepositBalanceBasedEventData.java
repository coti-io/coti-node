package io.coti.trustscore.data.tsbuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.tsevents.BalanceBasedEventData;
import lombok.Data;

@Data
public class BucketDepositBalanceBasedEventData extends BucketData<BalanceBasedEventData> implements IEntity {

    private static final long serialVersionUID = 1879770617138638423L;
    private double currentBalance = 0.0;
    private double currentBalanceContribution = 0.0;
    private double currentDayClose = 0.0;
    private double tail = 0.0;

    public BucketDepositBalanceBasedEventData() {
        super();
    }
}