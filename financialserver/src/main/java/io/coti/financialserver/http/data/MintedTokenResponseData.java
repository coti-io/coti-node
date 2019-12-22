package io.coti.financialserver.http.data;

import io.coti.financialserver.data.MintingHistoryData;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintedTokenResponseData implements Serializable {

    private String hash;
    private Instant mintingTime;
    protected BigDecimal mintingAmount;
    private String mintingTransactionHash;
    private String feeTransactionHash;

    public MintedTokenResponseData() {
    }

    public MintedTokenResponseData(MintingHistoryData mintingHistoryData) {
        this.hash = mintingHistoryData.getHash().toString();
        this.mintingTime = mintingHistoryData.getMintingTime();
        this.mintingAmount = mintingHistoryData.getMintingAmount();
        this.mintingTransactionHash = mintingHistoryData.getMintingTransactionHash().toString();
        this.feeTransactionHash = mintingHistoryData.getFeeTransactionHash().toString();
    }
}
