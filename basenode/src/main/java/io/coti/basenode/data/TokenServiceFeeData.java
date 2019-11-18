package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenServiceFeeData extends OutputBaseTransactionData {

    private Hash signerHash;
    protected Hash tokenHash;
    protected BigDecimal tokenAmount;

    private TokenServiceFeeData() {
        super();
    }

    public TokenServiceFeeData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Hash tokenHash, BigDecimal tokenAmount, Instant createTime) {
        super(addressHash, currencyHash, amount, currencyHash, amount, createTime);
        this.signerHash = signerHash;
        this.tokenHash = tokenHash;
        this.tokenAmount = tokenAmount;
    }

}
