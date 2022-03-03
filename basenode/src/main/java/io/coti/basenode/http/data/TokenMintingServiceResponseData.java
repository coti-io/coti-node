package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenMintingData;
import io.coti.basenode.data.TokenMintingFeeBaseTransactionData;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenMintingServiceResponseData implements ITransactionResponseData {

    private String mintingCurrencyHash;
    private BigDecimal mintingAmount;
    private String receiverAddress;
    private Instant createTime;
    private BigDecimal feeAmount;
    private String signerHash;
    private SignatureData signature;

    public TokenMintingServiceResponseData(BaseTransactionData baseTransactionData) {
        TokenMintingData tokenMintingData = ((TokenMintingFeeBaseTransactionData) baseTransactionData).getServiceData();
        this.setMintingAmount(tokenMintingData.getMintingAmount());
        this.setMintingCurrencyHash(tokenMintingData.getMintingCurrencyHash().toString());
        this.setFeeAmount(tokenMintingData.getFeeAmount());
        this.setReceiverAddress(tokenMintingData.getReceiverAddress().toString());
        this.setCreateTime(tokenMintingData.getCreateTime());
        this.setSignerHash(tokenMintingData.getSignerHash().toString());
        this.setSignature(tokenMintingData.getSignature());
    }
}
