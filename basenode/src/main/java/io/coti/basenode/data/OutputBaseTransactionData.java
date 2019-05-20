package io.coti.basenode.data;

import lombok.Data;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public abstract class OutputBaseTransactionData extends BaseTransactionData {

    private static final long serialVersionUID = 5660628603489226186L;
    @Positive
    protected BigDecimal originalAmount;

    protected OutputBaseTransactionData() {
        super();
    }

    protected OutputBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Instant createTime) {
        super(addressHash, amount, createTime);
        this.setOriginalAmount(originalAmount);
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalStateException("Output transaction can not have negative amount");
        }
        this.amount = amount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        if (originalAmount == null || originalAmount.signum() <= 0) {
            throw new IllegalStateException("Original amount can not have non positive amount");
        }
        this.originalAmount = originalAmount;
    }
}
