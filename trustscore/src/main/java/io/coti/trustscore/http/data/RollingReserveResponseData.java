package io.coti.trustscore.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.RollingReserveData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import io.coti.basenode.http.data.TrustScoreNodeResultResponseData;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class RollingReserveResponseData implements Serializable {

    private String hash;
    private String amount;
    private String originalAmount;
    private String reducedAmount;
    private String addressHash;
    private String currencyHash;
    private Instant createTime;
    private String name;
    private List<TrustScoreNodeResultResponseData> rollingReserveTrustScoreNodeResult;

    public RollingReserveResponseData(RollingReserveData rollingReserveData) {
        this.hash = rollingReserveData.getHash().toString();
        this.amount = rollingReserveData.getAmount().toPlainString();
        this.originalAmount = rollingReserveData.getOriginalAmount().toPlainString();
        this.reducedAmount = rollingReserveData.getReducedAmount().toPlainString();
        this.addressHash = rollingReserveData.getAddressHash().toString();
        this.currencyHash = rollingReserveData.getCurrencyHash().toString();
        this.createTime = rollingReserveData.getCreateTime();
        this.name = BaseTransactionName.getName(RollingReserveData.class).name();
        this.rollingReserveTrustScoreNodeResult = new ArrayList<>();

        for (TrustScoreNodeResultData trustScoreNodeResultData : rollingReserveData.getTrustScoreNodeResult()) {
            rollingReserveTrustScoreNodeResult.add(new TrustScoreNodeResultResponseData(trustScoreNodeResultData));
        }
    }

}
