package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.TokenGenerationFeeResponseData;
import lombok.Data;

@Data
public class TokenGenerationFeeResponse extends BaseResponse {
    private TokenGenerationFeeResponseData tokenServiceFee;

    public TokenGenerationFeeResponse() {
    }

    public TokenGenerationFeeResponse(TokenGenerationFeeResponseData tokenGenerationFeeResponseData) {
        this.tokenServiceFee = tokenGenerationFeeResponseData;
    }
}
