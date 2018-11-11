package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FullNodeFeeData extends OutputBaseTransactionData {

    private FullNodeFeeData() {
        super();
    }

    public FullNodeFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, originalAmount, baseTransactionHash, signature, createTime);

    }

    public FullNodeFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Date createTime) {
        super(addressHash, amount, originalAmount, createTime);

    }

}
