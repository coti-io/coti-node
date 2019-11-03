package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;

public class GenerateTokenResponse extends BaseResponse {

    private String tokenHash;

    public GenerateTokenResponse(Hash tokenHash) {
        this.tokenHash = tokenHash.toString();
    }
}
