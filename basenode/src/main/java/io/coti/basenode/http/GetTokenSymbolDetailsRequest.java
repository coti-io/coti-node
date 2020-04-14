package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class GetTokenSymbolDetailsRequest extends Request implements ISignValidatable {

    @NotNull
    private @Valid Hash userHash;
    @NotEmpty
    private String symbol;
    @NotNull
    private Instant createTime;
    @NotNull
    private @Valid SignatureData signature;

    @Override
    public Hash getSignerHash() {
        return userHash;
    }
}
