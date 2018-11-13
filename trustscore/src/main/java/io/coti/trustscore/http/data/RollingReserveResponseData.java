package io.coti.trustscore.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.RollingReserveData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class RollingReserveResponseData {

    private String hash;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private String addressHash;
    private Date createTime;
    private String name;
    private List<TrustScoreNodeResultData> trustScoreNodeResultData;

    public RollingReserveResponseData(RollingReserveData rollingReserveData) {
        this.hash = rollingReserveData.getHash().toString();
        this.amount = rollingReserveData.getAmount();
        this.originalAmount = rollingReserveData.getOriginalAmount();
        this.addressHash = rollingReserveData.getAddressHash().toString();
        this.createTime = rollingReserveData.getCreateTime();
        this.name = BaseTransactionName.getName(RollingReserveData.class).name();
        this.trustScoreNodeResultData = rollingReserveData.getRollingReserveTrustScoreNodeResult();
    }

}
