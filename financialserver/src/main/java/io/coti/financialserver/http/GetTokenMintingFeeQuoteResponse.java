package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.MintingFeeQuoteResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenMintingFeeQuoteResponse extends BaseResponse {

    private MintingFeeQuoteResponseData mintingFeeQuote;

    public GetTokenMintingFeeQuoteResponse(MintingFeeQuoteResponseData mintingFeeQuote) {
        this.mintingFeeQuote = mintingFeeQuote;
    }
}
