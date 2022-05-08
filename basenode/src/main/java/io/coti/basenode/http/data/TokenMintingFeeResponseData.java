package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenMintingFeeResponseData extends TokenFeeResponseData {

    TokenMintingServiceResponseData tokenMintingServiceData;

    public TokenMintingFeeResponseData() {
        super();
    }

    public TokenMintingFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
        tokenMintingServiceData = new TokenMintingServiceResponseData(baseTransactionData);
    }
}
