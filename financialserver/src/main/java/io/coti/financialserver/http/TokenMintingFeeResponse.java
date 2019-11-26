package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.TokenMintingFeeResponseData;
import lombok.Data;

@Data
public class TokenMintingFeeResponse extends BaseResponse {
    private TokenMintingFeeResponseData tokenServiceFee;

    public TokenMintingFeeResponse() {
    }

    public TokenMintingFeeResponse(TokenMintingFeeResponseData tokenMintingFeeResponseData) {
        this.tokenServiceFee = tokenMintingFeeResponseData;
    }
}
