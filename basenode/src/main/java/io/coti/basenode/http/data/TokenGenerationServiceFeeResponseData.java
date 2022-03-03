package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenGenerationServiceFeeResponseData extends TokenServiceFeeResponseData {

    TokenGenerationServiceResponseData tokenGenerationServiceResponseData;

    public TokenGenerationServiceFeeResponseData() {
        super();
    }

    public TokenGenerationServiceFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
        tokenGenerationServiceResponseData = new TokenGenerationServiceResponseData(baseTransactionData);
    }
}
