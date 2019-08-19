package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class FullNodeFeeData extends OutputBaseTransactionData {

    private static final long serialVersionUID = 1268594199336836721L;

    private FullNodeFeeData() {
        super();
    }

    public FullNodeFeeData(Hash addressHash, Hash currencyHash, BigDecimal amount, Hash originalCurrencyHash, BigDecimal originalAmount, Instant createTime) {
        super(addressHash, currencyHash, amount, originalCurrencyHash, originalAmount, createTime);

    }

}
