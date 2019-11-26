package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenGenerationFeeBaseTransactionData extends TokenFeeBaseTransactionData {

    private static final long serialVersionUID = -7466306830924153999L;
    private TokenGenerationFeeDataInBaseTransaction serviceData;

    public TokenGenerationFeeBaseTransactionData() {
    }

    public TokenGenerationFeeBaseTransactionData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Instant createTime, TokenGenerationFeeDataInBaseTransaction tokenGenerationFeeDataInBaseTransaction) {
        super(addressHash, currencyHash, signerHash, amount, createTime);
        this.serviceData = tokenGenerationFeeDataInBaseTransaction;

    }
}
