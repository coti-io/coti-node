package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ReceiverBaseTransactionData extends OutputBaseTransactionData {

    private Hash receiverDescription;

    private ReceiverBaseTransactionData() {
        super();
    }

    public ReceiverBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Instant createTime) {
        super(addressHash, amount, originalAmount, createTime);
    }

}
