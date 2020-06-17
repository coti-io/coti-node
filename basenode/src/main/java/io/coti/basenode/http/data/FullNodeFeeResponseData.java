package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FullNodeFeeResponseData extends OutputBaseTransactionResponseData {

    public FullNodeFeeResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
    }
}
