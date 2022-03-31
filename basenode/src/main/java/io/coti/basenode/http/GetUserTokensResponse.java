package io.coti.basenode.http;

import io.coti.basenode.http.data.TokenResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUserTokensResponse extends BaseResponse {

    private Set<TokenResponseData> userTokens;

    public GetUserTokensResponse() {
        userTokens = new HashSet<>();
    }
}
