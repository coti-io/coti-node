package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SetUserZeroTrustFlagRequest extends Request {

    private static final long serialVersionUID = 8385199133967348787L;
    @NotNull
    private Hash userHash;
    @NotNull
    private boolean zeroTrustFlag;
}

