package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.RollingReserveData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class RollingReserveResponseData extends OutputBaseTransactionResponseData {
    private BigDecimal reducedAmount;
    private List<TrustScoreNodeResultResponseData> rollingReserveTrustScoreNodeResult;

    public RollingReserveResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);

        RollingReserveData rollingReserveData = (RollingReserveData) baseTransactionData;
        this.reducedAmount = rollingReserveData.getReducedAmount();
        this.rollingReserveTrustScoreNodeResult = new ArrayList<>();

        for (TrustScoreNodeResultData trustScoreNodeResultData : rollingReserveData.getTrustScoreNodeResult()) {
            rollingReserveTrustScoreNodeResult.add(new TrustScoreNodeResultResponseData(trustScoreNodeResultData));
        }
    }
}
