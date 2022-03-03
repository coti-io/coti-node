package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenMintingServiceFeeResponseData extends TokenServiceFeeResponseData {

    TokenMintingServiceResponseData tokenMintingServiceResponseData;

    public TokenMintingServiceFeeResponseData() {
        super();
    }

    public TokenMintingServiceFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
        tokenMintingServiceResponseData = new TokenMintingServiceResponseData(baseTransactionData);
    }
}
