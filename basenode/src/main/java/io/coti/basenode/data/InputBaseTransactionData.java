package io.coti.basenode.data;


import lombok.Data;

import javax.validation.constraints.NegativeOrZero;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class InputBaseTransactionData extends BaseTransactionData {
    @NegativeOrZero
    private BigDecimal amount;

    private InputBaseTransactionData() {
        super();
    }

    public InputBaseTransactionData(Hash addressHash, BigDecimal amount, Date createTime) {
        super(addressHash, amount, createTime);
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() > 0) {
            throw new IllegalStateException("Input transaction can not have positive amount");
        }
        this.amount = amount;
    }

}
