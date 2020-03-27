package io.coti.financialserver.http.data;

import io.coti.basenode.data.*;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenGenerationFeeResponseData implements IResponse {

    private String hash;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private String addressHash;
    private String currencyHash;
    private TokenOriginatorCurrencyResponseData originatorCurrencyData;
    private TokenCurrencyTypeResponseData currencyTypeData;
    private String signerHash;
    private Instant createTime;
    private String name;
    private SignatureData signatureData;

    public TokenGenerationFeeResponseData(TokenFeeBaseTransactionData tokenFeeBaseTransactionData) {
        this.hash = tokenFeeBaseTransactionData.getHash().toString();
        this.amount = tokenFeeBaseTransactionData.getAmount();
        this.originalAmount = tokenFeeBaseTransactionData.getOriginalAmount();
        this.addressHash = tokenFeeBaseTransactionData.getAddressHash().toString();
        this.currencyHash = tokenFeeBaseTransactionData.getCurrencyHash().toString();
        this.originatorCurrencyData = new TokenOriginatorCurrencyResponseData((TokenGenerationData) tokenFeeBaseTransactionData.getServiceData());
        this.currencyTypeData = new TokenCurrencyTypeResponseData((TokenGenerationData) tokenFeeBaseTransactionData.getServiceData());
        this.signerHash = tokenFeeBaseTransactionData.getSignerHash().toString();
        this.createTime = tokenFeeBaseTransactionData.getCreateTime();
        this.name = BaseTransactionName.getName(TokenGenerationFeeBaseTransactionData.class).name();
        this.signatureData = tokenFeeBaseTransactionData.getSignatureData();
    }
}
