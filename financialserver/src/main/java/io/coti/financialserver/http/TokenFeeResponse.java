package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.TokenServiceFeeResponseData;
import lombok.Data;

@Data
public class TokenFeeResponse extends BaseResponse {
    private TokenServiceFeeResponseData tokenServiceFee;

    public TokenFeeResponse() {
    }

    public TokenFeeResponse(TokenServiceFeeResponseData tokenServiceFeeResponseData) {
        this.tokenServiceFee = tokenServiceFeeResponseData;
    }
}
