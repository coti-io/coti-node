package io.coti.financialserver.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenServiceFeeData;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenServiceFeeResponseData implements IResponse {

    private String hash;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private String addressHash;
    private String currencyHash;
    private String tokenHash;
    private BigDecimal tokenAmount;
    private String signerHash;
    private Instant createTime;
    private String name;
    private SignatureData signatureData;

    public TokenServiceFeeResponseData(TokenServiceFeeData tokenServiceFeeData) {
        this.hash = tokenServiceFeeData.getHash().toString();
        this.amount = tokenServiceFeeData.getAmount();
        this.originalAmount = tokenServiceFeeData.getOriginalAmount();
        this.addressHash = tokenServiceFeeData.getAddressHash().toString();
        this.currencyHash = tokenServiceFeeData.getCurrencyHash().toString();
        this.tokenHash = tokenServiceFeeData.getTokenHash().toString();
        this.tokenAmount = tokenServiceFeeData.getTokenAmount();
        this.signerHash = tokenServiceFeeData.getSignerHash().toString();
        this.createTime = tokenServiceFeeData.getCreateTime();
        this.name = BaseTransactionName.getName(TokenServiceFeeData.class).name();
        this.signatureData = tokenServiceFeeData.getSignatureData();
    }
}
