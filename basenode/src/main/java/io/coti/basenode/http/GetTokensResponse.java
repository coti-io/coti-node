package io.coti.basenode.http;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetTokensResponse extends BaseResponse{

    private List<GetTokenResponseData> tokensData;

    public GetTokensResponse() {
        this.tokensData = new ArrayList<>();
    }
}
