package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ITokenServiceData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TokenFeeBaseTransactionData extends OutputBaseTransactionData {

    private static final long serialVersionUID = -605143759679046209L;
    @NotNull
    private @Valid Hash signerHash;

    protected TokenFeeBaseTransactionData() {
        super();
    }

    protected TokenFeeBaseTransactionData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Instant createTime) {
        super(addressHash, currencyHash, amount, currencyHash, amount, createTime);
        this.signerHash = signerHash;
    }

    public abstract ITokenServiceData getServiceData();
}
