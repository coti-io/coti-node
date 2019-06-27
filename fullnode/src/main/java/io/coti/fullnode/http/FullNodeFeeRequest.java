package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class FullNodeFeeRequest extends Request implements ISignValidatable {
    @Positive
    private BigDecimal originalAmount;
    @NotNull
    private @Valid Hash userHash;
    @NotNull
    private @Valid SignatureData userSignature;

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }
}
