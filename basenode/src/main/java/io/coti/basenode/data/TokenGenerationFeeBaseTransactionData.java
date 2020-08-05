package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenGenerationFeeBaseTransactionData extends TokenFeeBaseTransactionData {

    private static final long serialVersionUID = -7466306830924153999L;
    private TokenGenerationData serviceData;

    private TokenGenerationFeeBaseTransactionData() {
    }

    public TokenGenerationFeeBaseTransactionData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Instant createTime, TokenGenerationData tokenGenerationData) {
        super(addressHash, currencyHash, signerHash, amount, createTime);
        this.serviceData = tokenGenerationData;
    }
}
