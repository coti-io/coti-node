package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.OutputBaseTransactionData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public abstract class OutputBaseTransactionResponseData extends BaseTransactionResponseData{
    protected BigDecimal originalAmount;

    protected OutputBaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);

        this.originalAmount = ((OutputBaseTransactionData) baseTransactionData).getOriginalAmount();
    }

}
