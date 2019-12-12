package io.coti.financialserver.http.data;

import io.coti.financialserver.data.MintingHistoryData;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class MintedTokenHistoryResponseData {

    private Map<Instant, MintedTokenResponseData> mintedTokenHistory;

    public MintedTokenHistoryResponseData() {
        this.mintedTokenHistory = new HashMap<>();
    }

    public MintedTokenHistoryResponseData(Map<Instant, MintingHistoryData> mintedTokenHistoryMap) {
        this.mintedTokenHistory = new HashMap<>();
        mintedTokenHistoryMap.entrySet().forEach(instantMintedTokenDataEntry -> {
            mintedTokenHistory.put(instantMintedTokenDataEntry.getKey(), new MintedTokenResponseData(instantMintedTokenDataEntry.getValue()));
        });
    }
}
