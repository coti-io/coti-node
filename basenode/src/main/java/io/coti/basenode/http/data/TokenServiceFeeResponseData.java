package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TokenFeeBaseTransactionData;
import lombok.Data;

@Data
public class TokenServiceFeeResponseData extends OutputBaseTransactionResponseData {

    private String signerHash;

    public TokenServiceFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
        signerHash = ((TokenFeeBaseTransactionData) baseTransactionData).getSignerHash().toString();
    }
}
