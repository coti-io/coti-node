package io.coti.basenode.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FullNodeFeeData extends OutputBaseTransactionData {

    public FullNodeFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, originalAmount, baseTransactionHash, signature, createTime);

    }
}
