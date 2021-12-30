package io.coti.financialserver.http.data;

import io.coti.basenode.data.*;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenMintingFeeResponseData implements IResponseData {

    private String hash;
    private BigDecimal amount;
    private String currencyHash;
    private String addressHash;
    private BigDecimal originalAmount;
    private String originalCurrencyHash;
    private TokenMintingResponseData serviceData;
    private String signerHash;
    private Instant createTime;
    private String name;
    private SignatureData signatureData;

    public TokenMintingFeeResponseData(TokenFeeBaseTransactionData tokenFeeBaseTransactionData) {
        this.hash = tokenFeeBaseTransactionData.getHash().toString();
        this.amount = tokenFeeBaseTransactionData.getAmount();
        this.currencyHash = tokenFeeBaseTransactionData.getCurrencyHash() != null ? tokenFeeBaseTransactionData.getCurrencyHash().toString() : null;
        this.addressHash = tokenFeeBaseTransactionData.getAddressHash().toString();
        this.originalAmount = tokenFeeBaseTransactionData.getOriginalAmount();
        this.originalCurrencyHash = tokenFeeBaseTransactionData.getOriginalCurrencyHash() != null ? tokenFeeBaseTransactionData.getOriginalCurrencyHash().toString() : null;
        this.serviceData = new TokenMintingResponseData((TokenMintingData) tokenFeeBaseTransactionData.getServiceData());
        this.signerHash = tokenFeeBaseTransactionData.getSignerHash().toString();
        this.createTime = tokenFeeBaseTransactionData.getCreateTime();
        this.name = BaseTransactionName.getName(TokenMintingFeeBaseTransactionData.class).name();
        this.signatureData = tokenFeeBaseTransactionData.getSignatureData();
    }
}
