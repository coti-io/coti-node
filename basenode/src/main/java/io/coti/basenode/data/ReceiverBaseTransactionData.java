package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ReceiverBaseTransactionData extends OutputBaseTransactionData {

    private Hash receiverDescription;

    private ReceiverBaseTransactionData() {
        super();
    }

    public ReceiverBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Date createTime) {
        super(addressHash, amount, originalAmount, createTime);
    }

}
