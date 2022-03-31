package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenMintingServiceData;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenMintingResponseData implements Serializable {

    private String mintingCurrencyHash;
    private BigDecimal mintingAmount;
    private String receiverAddress;
    private Instant createTime;
    private BigDecimal feeAmount;
    private String signerHash;
    private SignatureData signature;

    public TokenMintingResponseData(TokenMintingServiceData tokenMintingServiceData) {
        this.mintingCurrencyHash = tokenMintingServiceData.getMintingCurrencyHash().toString();
        this.mintingAmount = tokenMintingServiceData.getMintingAmount();
        this.receiverAddress = tokenMintingServiceData.getReceiverAddress().toString();
        this.createTime = tokenMintingServiceData.getCreateTime();
        this.feeAmount = tokenMintingServiceData.getFeeAmount();
        this.signerHash = tokenMintingServiceData.getSignerHash().toString();
        this.signature = tokenMintingServiceData.getSignature();
    }
}
