package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.MintedTokenData;
import io.coti.financialserver.http.data.MintedTokenHistoryResponseData;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class GetMintingHistoryResponse extends BaseResponse {

    private Map<String, MintedTokenHistoryResponseData> mintingHistory;

    public GetMintingHistoryResponse() {
        this.mintingHistory = new HashMap<>();
    }

    public GetMintingHistoryResponse(Map<Hash, Map<Instant, MintedTokenData>> mintingHistory) {

        this.mintingHistory = new HashMap<>();
        mintingHistory.entrySet().forEach(entry ->
                this.mintingHistory.put(entry.getKey().toString(), new MintedTokenHistoryResponseData(entry.getValue()))
        );
    }
}
