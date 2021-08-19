package io.coti.basenode.http;

import io.coti.basenode.http.data.TokenGenerationResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUserTokensResponse extends BaseResponse {

    private Set<TokenGenerationResponseData> userTokens;

    public GetUserTokensResponse() {
        userTokens = new HashSet<>();
    }
}
