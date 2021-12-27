package io.coti.financialserver.http.data;

import io.coti.basenode.http.data.interfaces.IResponseData;
import io.coti.financialserver.data.MintingHistoryData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintedTokenResponseData implements IResponseData {

    private String hash;
    private Instant mintingTime;
    protected BigDecimal mintingAmount;
    private String mintingTransactionHash;
    private String feeTransactionHash;

    public MintedTokenResponseData(MintingHistoryData mintingHistoryData) {
        this.hash = mintingHistoryData.getHash().toString();
        this.mintingTime = mintingHistoryData.getMintingTime();
        this.mintingAmount = mintingHistoryData.getMintingAmount();
        this.mintingTransactionHash = mintingHistoryData.getMintingTransactionHash().toString();
        this.feeTransactionHash = mintingHistoryData.getFeeTransactionHash().toString();
    }
}
