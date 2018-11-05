package io.coti.basenode.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class FullNodeFeeData extends BaseTransactionData {
    private BigDecimal originalAmount;
    protected OutputBaseTransactionType type = OutputBaseTransactionType.FullNodeFee;

    public FullNodeFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount,Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, baseTransactionHash, signature, createTime);
        this.originalAmount = originalAmount;
    }
}
