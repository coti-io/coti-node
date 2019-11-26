package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IServiceDataInBaseTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public abstract class TokenFeeBaseTransactionData extends OutputBaseTransactionData {

    private Hash signerHash;

    public TokenFeeBaseTransactionData() {
        super();
    }

    public TokenFeeBaseTransactionData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Instant createTime) {
        super(addressHash, currencyHash, amount, currencyHash, amount, createTime);
        this.signerHash = signerHash;
    }

    public IServiceDataInBaseTransaction getServiceData() {
        return null;
    }
}
