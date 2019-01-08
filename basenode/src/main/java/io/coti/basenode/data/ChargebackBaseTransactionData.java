package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ChargebackBaseTransactionData extends OutputBaseTransactionData {

    private ChargebackBaseTransactionData() {
        super();
    }

    public ChargebackBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Date createTime) {
        super(addressHash, amount, originalAmount, createTime);
    }

}
