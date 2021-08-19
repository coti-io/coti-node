package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReducedExistingBaseTransactionData {

    private Hash addressHash;
    private Hash currencyHash;
    private BigDecimal amount;

    public ReducedExistingBaseTransactionData(BaseTransactionData baseTransactionData) {
        this.addressHash = baseTransactionData.getAddressHash();
        this.currencyHash = baseTransactionData.getCurrencyHash();
        this.amount = baseTransactionData.getAmount();
    }
}
