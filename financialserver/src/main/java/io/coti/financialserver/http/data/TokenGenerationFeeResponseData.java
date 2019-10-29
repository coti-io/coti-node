package io.coti.financialserver.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenServiceFeeData;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.Data;

import java.time.Instant;

@Data
public class TokenGenerationFeeResponseData implements IResponse {

    private String hash;
    private String amount;
    private String originalAmount;
    private String addressHash;
    private String signerHash;
    private Instant createTime;
    private String name;
    private SignatureData signatureData;

    public TokenGenerationFeeResponseData(TokenServiceFeeData tokenServiceFeeData) {
        this.hash = tokenServiceFeeData.getHash().toString();
        this.amount = tokenServiceFeeData.getAmount().toPlainString();
        this.originalAmount = tokenServiceFeeData.getOriginalAmount().toPlainString();
        this.addressHash = tokenServiceFeeData.getAddressHash().toString();
        this.signerHash = tokenServiceFeeData.getSignerHash().toString();
        this.createTime = tokenServiceFeeData.getCreateTime();
        this.name = BaseTransactionName.getName(TokenServiceFeeData.class).name();
        this.signatureData = tokenServiceFeeData.getSignatureData();
    }
}
