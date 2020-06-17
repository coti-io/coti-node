package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.OutputBaseTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class OutputBaseTransactionResponseData extends BaseTransactionResponseData {
    protected BigDecimal originalAmount;

    protected OutputBaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);

        this.originalAmount = ((OutputBaseTransactionData) baseTransactionData).getOriginalAmount();
    }

}
