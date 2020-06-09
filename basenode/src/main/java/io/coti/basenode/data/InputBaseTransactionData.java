package io.coti.basenode.data;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class InputBaseTransactionData extends BaseTransactionData {

    private static final long serialVersionUID = -4057207259434521625L;

    protected InputBaseTransactionData() {
        super();
    }

    public InputBaseTransactionData(Hash addressHash, BigDecimal amount, Instant createTime) {
        super(addressHash, amount, createTime);
    }

    @Override
    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() > 0) {
            throw new IllegalStateException("Input transaction can not have positive amount");
        }
        this.amount = amount;
    }

}
