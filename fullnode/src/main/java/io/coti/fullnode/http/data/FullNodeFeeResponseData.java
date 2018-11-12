package io.coti.fullnode.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.SignatureData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FullNodeFeeResponseData {
    private String hash;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private String addressHash;
    private Date createTime;
    private String name;
    private SignatureData signatureData;

    public FullNodeFeeResponseData(FullNodeFeeData fullNodeFeeData) {
        this.hash = fullNodeFeeData.getHash().toString();
        this.amount = fullNodeFeeData.getAmount();
        this.originalAmount = fullNodeFeeData.getOriginalAmount();
        this.addressHash = fullNodeFeeData.getAddressHash().toString();
        this.createTime = fullNodeFeeData.getCreateTime();
        this.name = BaseTransactionName.getName(FullNodeFeeData.class).name();
        this.signatureData = fullNodeFeeData.getSignatureData();
    }
}
