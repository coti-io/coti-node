package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ReceiverBaseTransactionData extends OutputBaseTransactionData {

    private ReceiverBaseTransactionData() {
        super();
    }

    public ReceiverBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, originalAmount, baseTransactionHash, signature, createTime);
    }
}
