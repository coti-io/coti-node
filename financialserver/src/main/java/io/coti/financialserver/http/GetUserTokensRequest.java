package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;

@Data
public class GetUserTokensRequest extends Request implements ISignValidatable {

    @NotEmpty
    private @Valid Hash userHash;
    @NotEmpty
    private Instant creationTime;
    @NotEmpty
    private @Valid SignatureData signature;

    @Override
    public Hash getSignerHash() {
        return userHash;
    }
}
