package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenMintingData;
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

    public TokenMintingResponseData(TokenMintingData tokenMintingData) {
        this.mintingCurrencyHash = tokenMintingData.getMintingCurrencyHash().toString();
        this.mintingAmount = tokenMintingData.getMintingAmount();
        this.receiverAddress = tokenMintingData.getReceiverAddress().toString();
        this.createTime = tokenMintingData.getCreateTime();
        this.feeAmount = tokenMintingData.getFeeAmount();
        this.signerHash = tokenMintingData.getSignerHash().toString();
        this.signature = tokenMintingData.getSignature();
    }
}
