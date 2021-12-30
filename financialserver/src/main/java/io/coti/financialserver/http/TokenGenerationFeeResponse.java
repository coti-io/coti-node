package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.TokenGenerationFeeResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenGenerationFeeResponse extends BaseResponse {

    private TokenGenerationFeeResponseData tokenGenerationFee;

    private TokenGenerationFeeResponse() {
    }

    public TokenGenerationFeeResponse(TokenGenerationFeeResponseData tokenGenerationFeeResponseData) {
        this.tokenGenerationFee = tokenGenerationFeeResponseData;
    }
}
