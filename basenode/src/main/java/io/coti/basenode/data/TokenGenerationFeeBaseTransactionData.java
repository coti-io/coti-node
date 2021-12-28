package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenGenerationFeeBaseTransactionData extends TokenFeeBaseTransactionData {

    private static final long serialVersionUID = -7466306830924153999L;
    @NotNull
    private @Valid TokenGenerationData serviceData;

    private TokenGenerationFeeBaseTransactionData() {
        super();
    }

    public TokenGenerationFeeBaseTransactionData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Instant createTime, TokenGenerationData tokenGenerationData) {
        super(addressHash, currencyHash, signerHash, amount, createTime);
        this.serviceData = tokenGenerationData;
    }
}
