package io.coti.basenode.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class FullNodeFeeData extends BaseTransactionData {
    @Positive
    private BigDecimal amount;
    private BigDecimal originalAmount;
    protected final OutputBaseTransactionType type = OutputBaseTransactionType.FullNodeFee;

    public FullNodeFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount,Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, baseTransactionHash, signature, createTime);
        this.originalAmount = originalAmount;
    }
}
