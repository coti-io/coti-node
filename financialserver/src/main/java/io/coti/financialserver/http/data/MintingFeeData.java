package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintingFeeData {

    @NotNull
    protected Hash currencyHash;
    @Positive
    protected BigDecimal amount;
    @NotNull
    private Instant creationTime;

}
