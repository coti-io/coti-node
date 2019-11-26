package io.coti.financialserver.http.data;

import io.coti.financialserver.data.MintedTokenData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintedTokenResponseData {

    private String hash;
    private Instant mintingTime;
    protected BigDecimal mintingAmount;
    private String mintingTransactionHash;
    private String feeTransactionHash;

    public MintedTokenResponseData(MintedTokenData mintedTokenData) {
        this.hash = mintedTokenData.getHash().toString();
        this.mintingTime = mintedTokenData.getMintingTime();
        this.mintingAmount = mintedTokenData.getMintingAmount();
        this.mintingTransactionHash = mintedTokenData.getMintingTransactionHash().toString();
        this.feeTransactionHash = mintedTokenData.getFeeTransactionHash().toString();
    }
}
