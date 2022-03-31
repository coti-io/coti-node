package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenMintingFeeBaseTransactionData extends TokenFeeBaseTransactionData {

    private static final long serialVersionUID = 7641308128208586733L;
    @NotNull
    private @Valid TokenMintingServiceData serviceData;

    private TokenMintingFeeBaseTransactionData() {
        super();
    }

    public TokenMintingFeeBaseTransactionData(Hash addressHash, Hash currencyHash, Hash signerHash, BigDecimal amount, Instant createTime, TokenMintingServiceData tokenMintingServiceData) {
        super(addressHash, currencyHash, signerHash, amount, createTime);
        this.serviceData = tokenMintingServiceData;
    }

}
