package io.coti.financialserver.http.data;

import io.coti.basenode.data.TokenMintingFeeDataInBaseTransaction;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenMintingFeeDataResponseDetails implements Serializable {

    private String mintingCurrencyHash;
    private BigDecimal mintingAmount;
    private String receiverAddress;
    private Instant requestTime;
    private BigDecimal feeQuote;

    public TokenMintingFeeDataResponseDetails(TokenMintingFeeDataInBaseTransaction tokenMintingFeeDataInBaseTransaction) {
        this.mintingCurrencyHash = tokenMintingFeeDataInBaseTransaction.getMintingCurrencyHash().toString();
        this.mintingAmount = tokenMintingFeeDataInBaseTransaction.getMintingAmount();
        this.receiverAddress = tokenMintingFeeDataInBaseTransaction.getReceiverAddress().toString();
        this.requestTime = tokenMintingFeeDataInBaseTransaction.getRequestTime();
        this.feeQuote = tokenMintingFeeDataInBaseTransaction.getFeeQuote();
    }
}
