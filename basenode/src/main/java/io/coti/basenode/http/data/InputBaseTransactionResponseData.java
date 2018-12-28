package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;

@Data
public class InputBaseTransactionResponseData extends  BaseTransactionResponseData{

    public InputBaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
    }
}
