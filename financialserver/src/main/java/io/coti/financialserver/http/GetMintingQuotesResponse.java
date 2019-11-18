package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.GetMintingTokenQuoteData;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class GetMintingQuotesResponse extends BaseResponse {

    private Map<String, GetMintingTokenQuoteData> mintingFeeQuotes;

    public GetMintingQuotesResponse() {
        this.mintingFeeQuotes = new HashMap<>();
    }

    public GetMintingQuotesResponse(Map<String, GetMintingTokenQuoteData> mintingFeeQuotes) {
        this.mintingFeeQuotes = mintingFeeQuotes;
    }
}
