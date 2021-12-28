package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ReceiverBaseTransactionData extends OutputBaseTransactionData {

    private static final long serialVersionUID = 3401427617037616873L;
    private @Valid Hash receiverDescription;

    private ReceiverBaseTransactionData() {
        super();
    }

    public ReceiverBaseTransactionData(Hash addressHash, Hash currencyHash, BigDecimal amount, Hash originalCurrencyHash, BigDecimal originalAmount, Instant createTime) {
        super(addressHash, currencyHash, amount, originalCurrencyHash, originalAmount, createTime);
    }

}
