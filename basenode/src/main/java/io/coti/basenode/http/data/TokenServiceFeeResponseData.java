package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;

@Data
public class TokenServiceFeeResponseData extends OutputBaseTransactionResponseData {

    public TokenServiceFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
    }
}
