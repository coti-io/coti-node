package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;

@Data
public class FullNodeFeeResponseData extends OutputBaseTransactionResponseData{

    public FullNodeFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
    }
}
