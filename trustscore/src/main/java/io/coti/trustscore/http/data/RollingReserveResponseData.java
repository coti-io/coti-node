package io.coti.trustscore.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.RollingReserveData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class RollingReserveResponseData {

    private String hash;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private BigDecimal reducedAmount;
    private String addressHash;
    private Date createTime;
    private String name;
    private List<TrustScoreNodeResultResponseData> rollingReserveTrustScoreNodeResult;

    public RollingReserveResponseData(RollingReserveData rollingReserveData) {
        this.hash = rollingReserveData.getHash().toString();
        this.amount = rollingReserveData.getAmount();
        this.originalAmount = rollingReserveData.getOriginalAmount();
        this.reducedAmount = rollingReserveData.getReducedAmount();
        this.addressHash = rollingReserveData.getAddressHash().toString();
        this.createTime = rollingReserveData.getCreateTime();
        this.name = BaseTransactionName.getName(RollingReserveData.class).name();
        this.rollingReserveTrustScoreNodeResult = new ArrayList<>();

        for (TrustScoreNodeResultData trustScoreNodeResultData : rollingReserveData.getTrustScoreNodeResult()) {
            rollingReserveTrustScoreNodeResult.add(new TrustScoreNodeResultResponseData(trustScoreNodeResultData));
        }
    }

}
