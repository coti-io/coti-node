package io.coti.fullnode.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.SignatureData;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class FullNodeFeeResponseData implements Serializable {

    private static final long serialVersionUID = -5923558615226036336L;

    private String hash;
    private String amount;
    private String originalAmount;
    private String addressHash;
    private String currencyHash;
    private String originalCurrencyHash;
    private Instant createTime;
    private String name;
    private SignatureData signatureData;

    public FullNodeFeeResponseData(FullNodeFeeData fullNodeFeeData) {
        this.hash = fullNodeFeeData.getHash().toString();
        this.amount = fullNodeFeeData.getAmount().toPlainString();
        this.originalAmount = fullNodeFeeData.getOriginalAmount().toPlainString();
        this.addressHash = fullNodeFeeData.getAddressHash().toString();
        this.currencyHash = fullNodeFeeData.getCurrencyHash().toString();
        this.originalCurrencyHash = fullNodeFeeData.getOriginalCurrencyHash().toString();
        this.createTime = fullNodeFeeData.getCreateTime();
        this.name = BaseTransactionName.getName(FullNodeFeeData.class).name();
        this.signatureData = fullNodeFeeData.getSignatureData();
    }
}
