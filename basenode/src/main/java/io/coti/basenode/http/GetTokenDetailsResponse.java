package io.coti.basenode.http;

import io.coti.basenode.http.data.TokenGenerationResponseData;
import lombok.Data;

@Data
public class GetTokenDetailsResponse extends BaseResponse {

    private TokenGenerationResponseData token;

}
