package io.coti.basenode.http;

import io.coti.basenode.http.data.TokenGenerationResponseData;
import lombok.Data;

import java.util.HashSet;

@Data
public class GetUserTokensDataResponse extends BaseResponse {

    private HashSet<TokenGenerationResponseData> userTokens;

    public GetUserTokensDataResponse() {
        userTokens = new HashSet<>();
    }
}
