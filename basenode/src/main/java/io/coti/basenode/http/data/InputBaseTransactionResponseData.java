package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InputBaseTransactionResponseData extends BaseTransactionResponseData {

    public InputBaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
    }
}
