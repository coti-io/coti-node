package io.coti.basenode.http;

import io.coti.basenode.http.data.TokenGenerationResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenDetailsResponse extends BaseResponse {

    private TokenGenerationResponseData token;

}
