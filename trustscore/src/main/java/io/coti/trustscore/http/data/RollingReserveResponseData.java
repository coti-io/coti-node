package io.coti.trustscore.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.RollingReserveData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import io.coti.basenode.http.data.TrustScoreNodeResultResponseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class RollingReserveResponseData {

    private String hash;
    private String amount;
    private String originalAmount;
    private String reducedAmount;
    private String addressHash;
    private Date createTime;
    private String name;
    private List<TrustScoreNodeResultResponseData> rollingReserveTrustScoreNodeResult;

    public RollingReserveResponseData(RollingReserveData rollingReserveData) {
        this.hash = rollingReserveData.getHash().toString();
        this.amount = rollingReserveData.getAmount().toString();
        this.originalAmount = rollingReserveData.getOriginalAmount().toString();
        this.reducedAmount = rollingReserveData.getReducedAmount().toString();
        this.addressHash = rollingReserveData.getAddressHash().toString();
        this.createTime = rollingReserveData.getCreateTime();
        this.name = BaseTransactionName.getName(RollingReserveData.class).name();
        this.rollingReserveTrustScoreNodeResult = new ArrayList<>();

        for (TrustScoreNodeResultData trustScoreNodeResultData : rollingReserveData.getTrustScoreNodeResult()) {
            rollingReserveTrustScoreNodeResult.add(new TrustScoreNodeResultResponseData(trustScoreNodeResultData));
        }
    }

}
