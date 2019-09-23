package io.coti.financialserver.http;

import io.coti.basenode.data.TokenGenerationTransactionData;
import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.GeneratedTokenResponseData;
import lombok.Data;

import java.util.HashSet;

@Data
public class GetTokenGenerationDataResponse extends BaseResponse {

    private HashSet<GeneratedTokenResponseData> generatedTokens;

    public GetTokenGenerationDataResponse() {
        generatedTokens = new HashSet<>();
    }
}
