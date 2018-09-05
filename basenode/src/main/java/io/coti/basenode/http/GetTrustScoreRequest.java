package io.coti.basenode.http;

import io.coti.basenode.data.Hash;

import javax.validation.constraints.NotNull;

public class GetTrustScoreRequest extends Request {
    @NotNull
    public Hash userHash;
}
