package io.coti.basenode.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public abstract class OutputBaseTransactionData extends BaseTransactionData {
    @Positive
    protected BigDecimal amount;
    @Positive
    protected BigDecimal originalAmount;

    public OutputBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, baseTransactionHash, signature, createTime);
        this.setOriginalAmount(originalAmount);
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException("Output transaction can not have non positive amount");
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
