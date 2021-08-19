package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Data
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
