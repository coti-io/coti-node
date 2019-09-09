package io.coti.trustscore.data.scorebuckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreevents.BalanceBasedScoreData;
import io.coti.trustscore.data.parameters.CounteragentBalanceContributionData;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketDebtBalanceBasedScoreData extends BucketData<BalanceBasedScoreData> implements IEntity {

    private static final long serialVersionUID = 1776978715421639588L;
    private Map<Hash, CounteragentBalanceContributionData> hashCounteragentBalanceContributionDataMap;

    public BucketDebtBalanceBasedScoreData() {
        super();
        hashCounteragentBalanceContributionDataMap = new ConcurrentHashMap<>();
    }
}