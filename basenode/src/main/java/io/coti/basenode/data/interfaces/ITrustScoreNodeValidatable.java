package io.coti.basenode.data.interfaces;

import io.coti.basenode.data.TrustScoreNodeResultData;

import java.util.List;

public interface ITrustScoreNodeValidatable {
    List<TrustScoreNodeResultData> getTrustScoreNodeResult();

    void setTrustScoreNodeResult(List<TrustScoreNodeResultData> trustScoreNodeResult);
}
