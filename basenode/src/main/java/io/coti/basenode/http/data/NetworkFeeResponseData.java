package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.NetworkFeeData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class NetworkFeeResponseData extends OutputBaseTransactionResponseData {
    private BigDecimal reducedAmount;
    private List<TrustScoreNodeResultResponseData> networkFeeTrustScoreNodeResult;

    public NetworkFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);

        NetworkFeeData networkFeeData = (NetworkFeeData) baseTransactionData;
        this.reducedAmount = networkFeeData.getReducedAmount();
        this.networkFeeTrustScoreNodeResult = new ArrayList<>();

        for (TrustScoreNodeResultData trustScoreNodeResultData : networkFeeData.getTrustScoreNodeResult()) {
            networkFeeTrustScoreNodeResult.add(new TrustScoreNodeResultResponseData(trustScoreNodeResultData));
        }

    }
}
