package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.TokenMintingFeeResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenMintingFeeResponse extends BaseResponse {

    private TokenMintingFeeResponseData tokenServiceFee;

    public TokenMintingFeeResponse(TokenMintingFeeResponseData tokenMintingFeeResponseData) {
        this.tokenServiceFee = tokenMintingFeeResponseData;
    }
}
