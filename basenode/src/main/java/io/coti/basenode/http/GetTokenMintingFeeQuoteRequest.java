package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenMintingFeeQuoteRequest extends Request implements ISignValidatable {

    @NotNull
    private @Valid Hash currencyHash;
    @Positive
    private BigDecimal mintingAmount;
    @NotNull
    private Instant createTime;
    @NotNull
    private @Valid Hash userHash;
    @NotNull
    private @Valid SignatureData signature;

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

}
