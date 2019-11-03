package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TokenServiceFeeData;
import lombok.Data;

@Data
public class TokenServiceFeeResponseData extends OutputBaseTransactionResponseData {

    private String signerHash;

    public TokenServiceFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
        signerHash = ((TokenServiceFeeData) baseTransactionData).getSignerHash().toString();
    }
}
