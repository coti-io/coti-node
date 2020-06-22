package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ReceiverBaseTransactionData extends OutputBaseTransactionData {

    private static final long serialVersionUID = 3401427617037616873L;
    private Hash receiverDescription;

    private ReceiverBaseTransactionData() {
        super();
    }

    public ReceiverBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Instant createTime) {
        super(addressHash, amount, originalAmount, createTime);
    }

}
