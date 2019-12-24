package io.coti.trustscore.data.tsbuckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.tsevents.BalanceBasedEventData;
import io.coti.trustscore.data.contributiondata.CounteragentBalanceContributionData;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketDebtBalanceBasedEventData extends BucketData<BalanceBasedEventData> implements IEntity {

    private static final long serialVersionUID = 1776978715421639588L;
    private Map<Hash, CounteragentBalanceContributionData> hashCounteragentBalanceContributionDataMap;

    public BucketDebtBalanceBasedEventData() {
        super();
        hashCounteragentBalanceContributionDataMap = new ConcurrentHashMap<>();
    }
}