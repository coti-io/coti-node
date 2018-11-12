package io.coti.trustscore.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.RollingReserveData;
import io.coti.basenode.data.SignatureData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RollingReserveResponseData {

    private String hash;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private String addressHash;
    private Date createTime;
    private String name;
    private SignatureData signatureData;

    public RollingReserveResponseData(RollingReserveData rollingReserveData) {
        this.hash = rollingReserveData.getHash().toString();
        this.amount = rollingReserveData.getAmount();
        this.originalAmount = rollingReserveData.getOriginalAmount();
        this.addressHash = rollingReserveData.getAddressHash().toString();
        this.createTime = rollingReserveData.getCreateTime();
        this.name = BaseTransactionName.getName(RollingReserveData.class).name();
        this.signatureData = rollingReserveData.getSignatureData();
    }

}
