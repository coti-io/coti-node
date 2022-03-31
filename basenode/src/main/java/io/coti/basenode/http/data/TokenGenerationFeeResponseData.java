package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenGenerationFeeResponseData extends TokenFeeResponseData {

    TokenGenerationServiceResponseData tokenGenerationServiceResponseData;

    public TokenGenerationFeeResponseData() {
        super();
    }

    public TokenGenerationFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
        tokenGenerationServiceResponseData = new TokenGenerationServiceResponseData(baseTransactionData);
    }
}
