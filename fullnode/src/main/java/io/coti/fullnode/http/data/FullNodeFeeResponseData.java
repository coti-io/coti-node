package io.coti.fullnode.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.time.Instant;

@Data
public class FullNodeFeeResponseData implements IResponseData {

    private String hash;
    private String amount;
    private String originalAmount;
    private String addressHash;
    private String currencyHash;
    private Instant createTime;
    private String name;
    private SignatureData signatureData;

    public FullNodeFeeResponseData(FullNodeFeeData fullNodeFeeData) {
        this.hash = fullNodeFeeData.getHash().toString();
        this.amount = fullNodeFeeData.getAmount().toPlainString();
        this.originalAmount = fullNodeFeeData.getOriginalAmount().toPlainString();
        this.addressHash = fullNodeFeeData.getAddressHash().toString();
        this.currencyHash = fullNodeFeeData.getCurrencyHash().toString();
        this.createTime = fullNodeFeeData.getCreateTime();
        this.name = BaseTransactionName.getName(FullNodeFeeData.class).name();
        this.signatureData = fullNodeFeeData.getSignatureData();
    }
}
