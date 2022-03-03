package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TokenFeeBaseTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenServiceFeeResponseData extends OutputBaseTransactionResponseData {

    private String signerHash;

    public TokenServiceFeeResponseData() {
        super();
    }

    public TokenServiceFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
        signerHash = ((TokenFeeBaseTransactionData) baseTransactionData).getSignerHash().toString();
    }
}
