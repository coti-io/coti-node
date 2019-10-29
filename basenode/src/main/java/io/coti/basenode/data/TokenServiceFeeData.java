package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenServiceFeeData extends OutputBaseTransactionData {

    private Hash signerHash;

    private TokenServiceFeeData() {
        super();
    }

    public TokenServiceFeeData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Instant createTime) {
        super(addressHash, currencyHash, amount, currencyHash, amount, createTime);
        this.signerHash = signerHash;
    }

}
